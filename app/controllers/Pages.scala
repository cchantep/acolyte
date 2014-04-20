package controllers

import play.api._
import play.api.mvc._

object Pages extends Controller {

  def welcome = Assets.at(path="/public", "index.html")

}
