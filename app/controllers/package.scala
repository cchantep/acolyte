import java.util.Date

import play.api.libs.json.{ Reads, __ }
import play.api.libs.functional.syntax._

import acolyte.Implicits._
import acolyte.{ RowList, RowList1 }
import acolyte.RowLists.{ rowList1, rowList2, rowList3 }

package object controllers {
  sealed trait Route { def pattern: String }
  case class UpdateRoute(pattern: String, res: UpdateResult) extends Route
  case class QueryRoute(pattern: String, res: QueryResult) extends Route

  sealed trait UpdateResult
  case class UpdateCount(count: Int) extends UpdateResult
  case class UpdateError(message: String) extends UpdateResult

  implicit val updateResultReads: Reads[UpdateResult] =
    (__ \ 'error).readNullable[String] flatMap {
      case Some(err) ⇒ Reads.pure(UpdateError(err))
      case _         ⇒ (__ \ 'updateCount).read[Int].map(UpdateCount)
    }

  sealed trait QueryResult
  case class QueryError(message: String) extends QueryResult
  case class ResultColumn(typ: Class[_], name: String)
  case class RowResult(rows: RowList[_ <: acolyte.Row]) extends QueryResult

  val TextCol = classOf[String]
  val NumberCol = classOf[Float]
  val DateCol = classOf[Date]

  implicit val columnReads: Reads[ResultColumn] = (
    (__ \ '_type).read[String] map {
      case "string" ⇒ TextCol
      case "float"  ⇒ NumberCol
      case "date"   ⇒ DateCol
    } and (__ \ 'name).read[String])(ResultColumn)

  @inline def dateFormat = new java.text.SimpleDateFormat("YYYY-MM-dd")

  @inline def convertCol[T](typ: Class[T], s: String): T = typ match {
    case TextCol   ⇒ s.asInstanceOf[T]
    case NumberCol ⇒ s.toFloat.asInstanceOf[T]
    case DateCol   ⇒ dateFormat.parse(s).asInstanceOf[T]
    case _         ⇒ sys.error(s"Unsupported column ($typ): $s")
  }

  @inline def rows1Reads[A](a: (Class[A], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(rowList1(a)) { (l, r) ⇒
        r match { case x :: Nil ⇒ l.append(convertCol(a._1, x)) }
      })
    }

  @inline def rows2Reads[A, B](a: (Class[A], String), b: (Class[B], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(rowList2(a, b)) { (l, r) ⇒
        r match {
          case x :: y :: Nil ⇒ l.append(
            convertCol(a._1, x), convertCol(b._1, y))
        }
      })
    }

  @inline def rows3Reads[A, B, C](a: (Class[A], String), b: (Class[B], String), c: (Class[C], String)): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      RowResult(raw.foldLeft(rowList3(a, b, c)) { (l, r) ⇒
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

  implicit def queryResultReads: Reads[QueryResult] =
    (__ \ 'error).readNullable[String] flatMap {
      case Some(err) ⇒ Reads.pure(QueryError(err))
      case _         ⇒ rowResultReads
    }

  val routeReads: Reads[Route] = (__ \ '_type).read[String] flatMap {
    case "update" ⇒ (
      (__ \ 'pattern).read[String] and (__ \ 'result).read[UpdateResult]
    )(UpdateRoute)
    case _ ⇒ (
      (__ \ 'pattern).read[String] and (__ \ 'result).read[QueryResult]
    )(QueryRoute)
  }
}
