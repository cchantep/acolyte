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

  implicit val columnReads: Reads[ResultColumn] = (
    (__ \ '_type).read[String] map {
      case "string" ⇒ classOf[String]
      case "float"  ⇒ classOf[Float]
      case "date"   ⇒ classOf[Date]
    } and (__ \ 'name).read[String])(ResultColumn)

  @inline def rows1Reads(list: RowList1[_, _]): Reads[QueryResult] =
    (__ \ 'rows).read[Seq[Seq[String]]] map { raw ⇒
      ???
    }

  private val rowResultReads: Reads[QueryResult] =
    (__ \ 'schema).read[Seq[ResultColumn]] flatMap {
      case a :: Nil ⇒
        val c: acolyte.Column[_] = a.typ -> a.name
        rows1Reads(rowList1(a.typ -> a.name))
      case a :: b :: Nil      ⇒ ???
      //rowsReads(rowList2(a.typ -> a.name, b.typ -> b.name))
      case a :: b :: c :: Nil ⇒ ???
      //rowsReads(rowList3(a.typ -> a.name, b.typ -> b.name, c.typ -> c.name))
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
