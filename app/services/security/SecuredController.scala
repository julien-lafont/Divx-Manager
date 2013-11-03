package services.security

import play.api.mvc._

sealed case class SecuredRequest[A](user: Identity, request: Request[A]) extends WrappedRequest(request)

trait SecuredController extends Controller {

  protected def Secured[A](p: BodyParser[A])(f: SecuredRequest[A] => Result) = {
    Action(p) { implicit request =>
      Identity.get.map { user =>
        f(SecuredRequest(user, request))
      }.getOrElse(Unauthorized)
    }
  }

  protected def Secured(f: SecuredRequest[AnyContent] => Result): Action[AnyContent]  = {
    Secured(parse.anyContent)(f)
  }

  implicit def securedRequestToIdentity(implicit request: SecuredRequest[_]) = request.user
}
