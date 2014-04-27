package controllers

import play.api.mvc.{ Action, Controller, SimpleResult }
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText }

import play.api.libs.json.{ Json, Reads }

object Pages extends Controller {

  def welcome = Assets.at(path = "/public", "index.html")

  sealed case class RouteData(json: String)

  def test = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[SimpleResult]({ f ⇒ Ok(s"f.errors") }, testWithData)

  }

  @inline def testWithData(data: RouteData): SimpleResult = {
    val js = Json.parse(data.json)
    val parser = Reads.seq(routeReads)
    Ok(s"Y: ${parser reads js}")
  }
}
