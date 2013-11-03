package controllers

import play.api._
import play.api.mvc._
import services.security.Identity

object Application extends Controller {

  def index(angularUri: String = "") = Action { implicit request =>
    Ok(views.html.index.render())
      .withSession("identity" -> Identity.get.map(_.name).get)
  }

  def unauthorized() = Action { request =>
    Unauthorized(s"Ce lieu de connexion n'a pas été autorisé. \nIP : ${request.remoteAddress}")
  }

  def logout() = Action { request =>
    Redirect("/").withNewSession
  }

}


object AppRouting extends Controller {

  def jsRoutes() = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Api.list,
        routes.javascript.Api.listDirs,
        routes.javascript.Api.roots,
        routes.javascript.Api.rawListing,
        routes.javascript.Api.download,
        routes.javascript.Api.newFiles,
        routes.javascript.Request.post,
        routes.javascript.Movie.detail
      )
    ).as(JAVASCRIPT)
  }

}
