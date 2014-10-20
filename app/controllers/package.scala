import java.util.Date

import reactivemongo.bson.BSONDocument

import play.api.libs.json.{ Reads, __ }
import play.api.libs.functional.syntax._

package object controllers {
  sealed trait RouteParameter
  case class StringParameter(name: String, value: String) extends RouteParameter
  case class FloatParameter(name: String, value: Float) extends RouteParameter
  case class DateParameter(name: String, value: Date) extends RouteParameter

  case class RoutePattern(col: String, params: Seq[RouteParameter])
  sealed trait Route { def pattern: RoutePattern }
  case class UpdateRoute(pattern: RoutePattern, res: UpdateResult) extends Route
  case class QueryRoute(pattern: RoutePattern, res: QueryResult) extends Route

  sealed trait Result

  sealed trait UpdateResult extends Result
  case class UpdateCount(count: Int) extends UpdateResult
  case class UpdateError(message: String) extends UpdateResult

  implicit val updateResultReads: Reads[UpdateResult] =
    (__ \ 'error).readNullable[String] flatMap {
      case Some(err) ⇒ Reads.pure(UpdateError(err))
      case _         ⇒ (__ \ 'updateCount).read[Int].map(UpdateCount)
    }

  sealed trait Row
  case class Row1[A](va: A) extends Row
  case class Row2[A, B](va: A, vb: B) extends Row
  case class Row3[A, B, C](va: A, vb: B, vc: C) extends Row

  type Column[T] = (Class[T], String)

  sealed trait RowList[R <: Row] {
    def rows: List[R]
    def columnLabels: List[String]
    def columnClasses: List[Class[_]]
  }
  object RowList {
    def list1[A](a: Column[A]) = RowList1(a, Nil)
    def list2[A, B](a: Column[A], b: Column[B]) = RowList2(a, b, Nil)

    def list3[A, B, C](a: Column[A], b: Column[B], c: Column[C]) =
      RowList3(a, b, c, Nil)
  }

  case class RowList1[A](a: Column[A], rows: List[Row1[A]])
      extends RowList[Row1[A]] {
    val columnClasses = List(a._1)
    val columnLabels = List(a._2)

    def append(va: A): RowList1[A] = copy(a, rows :+ Row1(va))
  }
  case class RowList2[A, B](a: Column[A], b: Column[B], rows: List[Row2[A, B]])
      extends RowList[Row2[A, B]] {
    val columnClasses = List(a._1, b._1)
    val columnLabels = List(a._2, b._2)

    def append(va: A, vb: B): RowList2[A, B] = copy(a, b, rows :+ Row2(va, vb))
  }
  case class RowList3[A, B, C](a: Column[A], b: Column[B], c: Column[C],
      rows: List[Row3[A, B, C]]) extends RowList[Row3[A, B, C]] {
    val columnClasses = List(a._1, b._1, c._1)
    val columnLabels = List(a._2, b._2, c._2)

    def append(va: A, vb: B, vc: C): RowList3[A, B, C] =
      copy(a, b, c, rows :+ Row3(va, vb, vc))
  }

  sealed trait QueryResult extends Result
  case class QueryError(message: String) extends QueryResult
  case class ResultColumn(typ: Class[_], name: String)
  case class RowResult(rows: RowList[_ <: Row]) extends QueryResult

  val TextCol = classOf[String]
  val NumberCol = classOf[Float]
  val DateCol = classOf[Date]

  implicit val columnReads: Reads[ResultColumn] = (
    (__ \ '_type).read[String] map {
      case "string" ⇒ TextCol
      case "float"  ⇒ NumberCol
      case "date"   ⇒ DateCol
    } and (__ \ 'name).read[String])(ResultColumn)

  @inline def DateFormat = new java.text.SimpleDateFormat(
    "yyyy-MM-dd", java.util.Locale.ENGLISH)

  @inline def convertCol[T](typ: Class[T], s: String): T = typ match {
    case TextCol   ⇒ s.asInstanceOf[T]
    case NumberCol ⇒ s.toFloat.asInstanceOf[T]
    case DateCol   ⇒ DateFormat.parse(s).asInstanceOf[T]
    case _         ⇒ sys.error(s"Unsupported column ($typ): $s")
  }

  @inline def rows1Reads[A](a: (Class[A], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(RowList.list1(a)) { (l, r) ⇒
        r match { case x :: Nil ⇒ l.append(convertCol(a._1, x)) }
      })
    }

  @inline def rows2Reads[A, B](a: (Class[A], String), b: (Class[B], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(RowList.list2(a, b)) { (l, r) ⇒
        r match {
          case x :: y :: Nil ⇒ l.append(
            convertCol(a._1, x), convertCol(b._1, y))
        }
      })
    }

  @inline def rows3Reads[A, B, C](a: (Class[A], String), b: (Class[B], String), c: (Class[C], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(RowList.list3(a, b, c)) { (l, r) ⇒
        r match {
          case x :: y :: z :: Nil ⇒ l.append(
            convertCol(a._1, x), convertCol(b._1, y), convertCol(c._1, z))
        }
      })
    }

  private val rowResultReads: Reads[QueryResult] =
    (__ \ 'schema).read[Seq[ResultColumn]] flatMap {
      case a :: Nil      ⇒ rows1Reads(a.typ -> a.name)
      case a :: b :: Nil ⇒ rows2Reads(a.typ -> a.name, b.typ -> b.name)
      case a :: b :: c :: Nil ⇒
        rows3Reads(a.typ -> a.name, b.typ -> b.name, c.typ -> c.name)
    }

  implicit val queryResultReads: Reads[QueryResult] =
    (__ \ 'error).readNullable[String] flatMap {
      case Some(err) ⇒ Reads.pure(QueryError(err))
      case _         ⇒ rowResultReads
    }

  @inline val paramReads: Reads[String] = (__ \ 'value).read[String]
  implicit val routeParamReads: Reads[RouteParameter] = for {
    n ← (__ \ 'name).read[String]
    t ← (__ \ '_type).read[String]
    v ← paramReads
  } yield (t match {
    case "float" ⇒ FloatParameter(n, v.toFloat)
    case "date"  ⇒ DateParameter(n, DateFormat parse v)
    case _       ⇒ StringParameter(n, v)
  })

  implicit val routePatternReads: Reads[RoutePattern] = (
    (__ \ 'expression).read[String] and
    (__ \ 'parameters).read[Seq[RouteParameter]]
  )(RoutePattern)

  val routeReads: Reads[Route] = (__ \ '_type).read[String] flatMap {
    case "update" ⇒ (
      (__ \ 'pattern).read[RoutePattern] and (__ \ 'result).read[UpdateResult]
    )(UpdateRoute)
    case _ ⇒ (
      (__ \ 'pattern).read[RoutePattern] and (__ \ 'result).read[QueryResult]
    )(QueryRoute)
  }
}
