package controllers

import play.api._
import play.api.mvc._
import services.security.Identity

object Application extends Controller {

  def index() = Action { implicit request =>
    Ok(views.html.index.render())
  }

  def unauthorized() = Action { request =>
    Unauthorized("Ce lieu de connexion n'a pas été autorisé. \nIP : %s" format(request.remoteAddress))
  }
}