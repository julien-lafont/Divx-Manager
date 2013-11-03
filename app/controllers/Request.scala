package controllers

import play.api._
import play.api.mvc._
import services.security.Identity
import play.api.data._
import play.api.data.Forms._
import com.typesafe.plugin._
import play.api.Play.current

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.data.validation.ValidationError

object Request extends Controller {

  import MediaRequest._

  def post = Action(parse.json) { implicit request =>
    request.body.validate[MediaRequest](MediaRequest.mediaRequestReader) match {
      case JsSuccess(mediaReq, _) =>
        sendRequestMail(mediaReq)
        Ok
      case e: JsError =>
        UnprocessableEntity(JsError.toFlatJson(e))
    }
  }

  //"WTF? Le formulaire n'est pas correctement remplis. Attention, au 3ème essai l'univers s'auto-détruira !"

  private def sendRequestMail(req: MediaRequest)(implicit httpRequest: RequestHeader) = {
    val identity = Identity.get.get
    val content = s"""
      <h1>Requête : ${req.titre}</h1>
      <p>Type : ${req.media}<br/>
        Qualité : ${req.qualite}<br />
        Langue : ${req.langue}<br />
        Com : ${req.com}<br />
        By : ${identity.name}</p>"""

    val mail = use[MailerPlugin].email
    mail.setSubject(s"[DivxManager] Requête : ${req.titre} by ${identity.name}")
    mail.addRecipient("yotsumi.fx+divxmanager@gmail.com")
    mail.addFrom("Robot DivxManager <robot.divxmanager@studio-dev.fr>")
    mail.sendHtml(s"<html><body>$content</body></html>")
  }
}

case class MediaRequest(media: String, titre: String, langue: String, qualite: String, com: String)

object MediaRequest {
  val notEmpty = Reads.filterNot[String](ValidationError("validate.error.empty"))(_.isEmpty)

  implicit val mediaRequestReader = (
    (__ \ "type").read[String](notEmpty) and
    (__ \ "titre").read[String](notEmpty) and
    (__ \ "langue").read[String](notEmpty) and
    (__ \ "qualite").read[String](notEmpty) and
    (__ \ "com").readNullable[String].map(_.getOrElse(""))
  )(MediaRequest.apply _)
}