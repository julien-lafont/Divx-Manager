package controllers

import scalax.io._
import play.api._
import play.api.mvc._

import services.security.Identity
import java.text.SimpleDateFormat

object Application extends Controller {

  val statsFile = Resource.fromFile("stats")

  def index(angularUri: String = "") = Action { implicit request =>
    val id = Identity.get.map(_.name).get
    log(id)

    Ok(views.html.index.render())
      .withSession("identity" -> id)
  }

  def unauthorized() = Action { request =>
    Unauthorized(s"Ce lieu de connexion n'a pas été autorisé. \nIP : ${request.remoteAddress}")
  }

  def logout() = Action { request =>
    Redirect("/").withNewSession
  }

  private def log(id: String)(implicit req: RequestHeader) = {
    val dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    statsFile.append(dateFormater.format(new java.util.Date()) + ";" + id + ";"+req.remoteAddress+"\n")(scalax.io.Codec.UTF8)
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
