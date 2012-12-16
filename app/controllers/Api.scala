package controllers

import play.api._
import play.api.mvc._
import scalax.file.Path
import scalax.file.ImplicitConversions._
import scalax.file.PathSet
import Path._
import scalax.file.PathMatcher._
import play.api.libs.json.Json
import play.api.libs.json._
import FileApi._
import scala.collection.JavaConversions._
import scala.math._
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import services.security.Identity
  
object Api extends Controller {
  
  val baseDir = "./links"
  val moviesExtensions = Seq("avi", "wmv", "mkv", "mp4", "mpg")

  def list(dir: String) = Action {
    val path = baseDir + dir
    val files = path **  ("*.{" + moviesExtensions.mkString(",") + "}")
    val order = files.toList.sortBy(_.name)
    Ok(Json.toJson(order))
  }

  def listDirs(dir: String) = Action {
    val path = baseDir + dir
    val folders = path.children(IsDirectory)
    val order = folders.toList.sortBy(_.name)
    Ok(Json.toJson(order))
  }

  def roots = Action { implicit request =>
    val identity = Identity.get
    val folders = identity.get.folders.map(f => Map(
        "dir" -> f.path, 
        "name" -> f.name, 
        "type" -> f.contentType.toString()))
    Ok(Json.toJson(folders))
  }

  def download(path: String) = Action {
    Ok.sendFile(new java.io.File(baseDir + path))
  }

}

object FileApi {

  private lazy val conf = play.api.Play.current.configuration.getConfig("config").get
  private val qualities = conf.getStringList("formats").get.toSeq
  private val langs = conf.getStringList("langs").get.toSeq
  private val badKeywords = conf.getStringList("blacklist").get.toSeq

  private val dateFormater = DateTimeFormat.shortDateTime.withLocale(Locale.FRANCE)

  implicit def pathWritter : Writes[Path] = new Writes[Path] {
    def writes(path: Path) = {
      Json.obj(
        "name"      -> getCleanName(path.name),
        "rawName" -> path.name,
        "path"  -> path.relativize(Api.baseDir).path,
        "extension" -> path.extension,
        "lastModified" -> dateFormater.print(path.lastModified),
        "size"      -> getSize(path.size),
        "isFile" -> path.isFile,
        "details" -> Json.obj(
          "quality"   -> getQuality(path.name),
          "lang"      -> getLang(path.name),
          "year"      -> getYear(path.name),
          "season"    -> getSeason(path.name),
          "episode"   -> getEpisode(path.name)
        )
      )
    }
  }

  private def getQuality(name: String) = {
    qualities.filter(quality => name.toLowerCase.contains(quality)).headOption.getOrElse("sd")
  }

  private def getLang(name: String) = {
    langs.filter(lang => 
      name.toLowerCase.contains("."+lang) || 
      name.toLowerCase.contains("-"+lang) ||
      name.toLowerCase.contains("["+lang)
    ).headOption.getOrElse("fr")
  }

  private def getYear(name: String) = {
    val regexYear = """.*\(([0-9]{4})\).*""".r
    name match {
      case regexYear(year) => Some(year)
      case _ => None
    }
  }

  private def getSeason(name: String) = {
    val regexSeason = """.*S([0-9]{1,2}).*""".r
    name.toUpperCase match {
      case regexSeason(season) => Some(season)
      case _ => None
    }
  }

  private def getEpisode(name: String) = {
    val regexEpisode = """.*S[0-9]{1,2}E([0-9]{1,2}).*""".r
    name.toUpperCase match {
      case regexEpisode(ep) => Some(ep)
      case _ => None
    }
  }

  private def getCleanName(name: String) = {
   val tmp = name.toLowerCase
       .replaceAll(Api.moviesExtensions.map("\\."+_).mkString("|"), "") // Remove extensions
       .replaceAll("""\[.*\]""", "") // Remove quality/lang
       .replaceAll("""\(.*\)""", "") // Remove year
       .replaceAll("""[-_\.]""", " ") // Replace special chars by space
       .replaceAll(badKeywords.mkString(" ", "| ", ""), " ") // Remove blacklisted keywords
       .replaceAll("""[ ]{1,99}""", " ") // Remove unnecessary spaces
       .trim
   tmp(0).toString.toUpperCase + tmp.drop(1) // Capitalize name
  }

  private def getSize(size: Option[Long]) = {
    size.map { s => 
      val mo = round(s / 1000d / 1000)
      if (mo > 1000) 
        BigDecimal(mo / 1000d).setScale(1, BigDecimal.RoundingMode.HALF_UP) + " go"
      else
        mo + " mo"
    }
  }
}