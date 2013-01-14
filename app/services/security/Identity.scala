package services.security

import play.api.mvc.RequestHeader
import scala.collection.JavaConversions._

sealed case class Identity(name: String, folders: List[Folder])
sealed case class Folder(name: String, path: String, contentType: FolderType)

sealed abstract class FolderType
case object Movie extends FolderType
case object TvShow extends FolderType
case object Music extends FolderType
case object Other extends FolderType

object Identity {

  lazy val authorizations = play.api.Play.current.configuration.getConfig("authorizations").get
  val empty = new java.util.ArrayList[String]()

  def get(implicit request: RequestHeader): Option[Identity] = {
    get(request.remoteAddress)
  }

  def get(ip: String): Option[Identity] = {
    authorizations.subKeys.filter(name => 
      authorizations.getConfig(name).map(conf =>
        conf.getStringList("ips").getOrElse(empty).toList.contains(ip)
      ).getOrElse(false))
    .headOption
    .map(identity => 
      Identity(
        name = identity, 
        folders = authorizations.getConfig(identity).get
          .getStringList("folders").getOrElse(empty).toList
          .map(folder => Folder(folder))
          .flatten
      )  
    )
  }

  def isFolderAuthorized(dir: String)(implicit request: RequestHeader): Boolean = {
    get.map(_.folders.filter(e => dir.split("/")(1) == e.path).size > 0).getOrElse(false)
  }
}

object Folder {
  lazy val config = play.api.Play.current.configuration.getConfig("folders").get

  def apply(name: String): Option[Folder] = {
    config.getConfig(name).map(folder =>
      Folder(
          folder.getString("name").get, 
          folder.getString("path").get, 
          folder.getString("type") match {
            case Some("movie") => Movie
            case Some("tvshow") => TvShow
            case Some("music") => Music
            case _ => Other
      })
    )
  }
}