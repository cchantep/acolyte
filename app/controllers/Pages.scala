package controllers

import play.api._
import play.api.mvc._

object Pages extends Controller {

  def welcome = Action { Ok(views.html.welcome()) }

  def step1 = Action { Ok(views.html.step1()) }

}
