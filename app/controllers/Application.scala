package controllers

import play.api._
import play.api.mvc._
import services.security.Identity
import play.api.data._
import play.api.data.Forms._
import com.typesafe.plugin._
import play.api.Play.current

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

  case class MediaRequest(media: String, titre: String, langue: String, qualite: String)

  /*def request() = Action { implicit request =>
    //Ok(views.html.request())
  }


  def handleRequest() = Action { implicit request =>
    val requestForm = Form(mapping("type" -> nonEmptyText, "titre" -> nonEmptyText, "langue" -> nonEmptyText, "qualite" -> nonEmptyText)(MediaRequest.apply)(MediaRequest.unapply))
    requestForm.bindFromRequest.fold(
      errors =>
        BadRequest(views.html.request(Some("WTF? Le formulaire n'est pas correctement remplis. Attention, au 3ème essai l'univers s'auto-détruira !"))),
      mediaReq => {
        sendRequestMail(mediaReq)
        Redirect(routes.Application.request).flashing("success" -> "Votre requête a été envoyée !")
      }
    )
  }*/

  private def sendRequestMail(req: MediaRequest)(implicit httpRequest: RequestHeader) = {
    val identity = Identity.get.get
    val content = s"<h1>Requête : ${req.titre}</h1>" +
      s"<p>Type : ${req.media}<br/>" +
      s"Qualité : ${req.qualite}<br />" +
      s"Langue : ${req.langue}<br />" +
      s"By : ${identity.name}</p>"

    val mail = use[MailerPlugin].email
    mail.setSubject(s"[DivxManager] Requête : ${req.titre} by ${identity.name}")
    mail.addRecipient("yotsumi.fx+divxmanager@gmail.com")
    mail.addFrom("Robot DivxManager <robot.divxmanager@studio-dev.fr>")
    mail.sendHtml(s"<html><body>$content</body></html>")
  }
}
