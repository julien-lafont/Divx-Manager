import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import services.security.Identity


object Global extends GlobalSettings {

  val noAuthPaths = Seq(
    "/unauthorized",
    "/logout"
  )

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
     if (noAuthPaths.contains(request.uri)) {
       super.onRouteRequest(request)
     }

     Identity.get(request).map(identity =>
       super.onRouteRequest(request)
     ).getOrElse(
       super.onRouteRequest(
         request.copy(
           path="/unauthorized",
           uri="/unauthorized",
           method="GET")))
  }

}
