package controllers

import play.api.mvc.{ Action, Controller, SimpleResult }
import play.api.data.Form
import play.api.data.Forms.{ mapping, nonEmptyText, optional }

import play.api.libs.json.{ Json, Reads }

object Acolyte extends Controller {

  def welcome = Assets.at(path = "/public", "index.html")

  sealed case class RouteData(json: String)

  def setup = Action { request ⇒
    Form[Option[RouteData]](mapping("json" -> optional(nonEmptyText))(
      { _.map(RouteData) })(_.map({ d ⇒ Some(d.json) }))).
      bindFromRequest()(request).fold[SimpleResult]({
        f ⇒ Ok(s"${f.errors}")
      }, { data ⇒
        Ok(views.html.setup(data.map(_.json)))
      })
  }

  def run = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[SimpleResult]({ f ⇒ Ok(s"${f.errors}") }, { data ⇒
        Ok(views.html.run(data.json))
      })
  }

  def executeStatement = Action { request ⇒
    Form(mapping("json" -> nonEmptyText)(
      RouteData.apply)(RouteData.unapply)).bindFromRequest()(request).
      fold[SimpleResult]({ f ⇒ Ok(s"f.errors") }, { data ⇒
        Reads.seq(routeReads).reads(Json.parse(data.json)).
          fold[SimpleResult]({ e ⇒ PreconditionFailed(e.mkString) },
            executeWithRoutes)
      })
  }

  @inline def executeWithRoutes(routes: Seq[Route]): SimpleResult = {
    println(s"routes = $routes")
    Ok(Json toJson true)
  }
}
