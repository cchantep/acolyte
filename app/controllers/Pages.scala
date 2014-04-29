package controllers

import play.api.mvc.{ Action, Controller, SimpleResult }
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText }

import play.api.libs.json.{ Json, Reads }

object Pages extends Controller {

  def welcome = Assets.at(path = "/public", "index.html")

  def setup = Action(Ok(views.html.setup()))

  sealed case class RouteData(json: String)

  def test = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[SimpleResult]({ f ⇒ Ok(s"f.errors") }, { data ⇒
        Reads.seq(routeReads).reads(Json.parse(data.json)).
          fold[SimpleResult]({ e ⇒ PreconditionFailed(e.mkString) },
            testWithRoutes)
      })
  }

  @inline def testWithRoutes(routes: Seq[Route]): SimpleResult =
    Ok(s"Routes: ${Json.toJson(routes)}")

}
