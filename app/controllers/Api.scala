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
import play.api.libs.functional.syntax._
import scala.collection.JavaConversions._
import scala.math._
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import services.security.Identity
import play.api.libs.iteratee._
import play.api.cache.Cache
import play.api.Play.current
  
object Api extends Controller {

  implicit val myFileDetailsWriter = (
    (__ \ "quality").write[String] and
    (__ \ "lang").write[String] and
    (__ \ "year").write[String] and
    (__ \ "season").write[String] and
    (__ \ "episode").write[String]
  )(unlift(MyFileDetails.unapply))

  implicit val myFileWriter = (
    (__ \ "name").write[String] and
    (__ \ "rawName").write[String] and
    (__ \ "path").write[String] and
    (__ \ "extension").write[String] and
    (__ \ "lastModified").write[String] and
    (__ \ "rawLastModified").write[Long] and
    (__ \ "size").write[String] and
    (__ \ "rawSize").write[Long] and
    (__ \ "isFile").write[Boolean] and
    (__ \ "details").write[MyFileDetails]
  )(unlift(MyFile.unapply))
  
  val baseDir = "./links"
  val moviesExtensions = Seq("avi", "wmv", "mkv", "mp4", "mpg")

  def list(dir: String) = Action { implicit request =>
    val json = Cache.getOrElse(getCacheKey(dir, "files"), 60*5){
      val path = baseDir + dir
      val files = path **  ("*.{" + moviesExtensions.mkString(",") + "}")
      val elems = sort(files.toList.map(MyFile(_)))
      Json.toJson(elems)
    }
    Ok(json)
  }

  def listDirs(dir: String) = Action { implicit request =>
    val json = Cache.getOrElse(getCacheKey(dir, "dirs"), 60*5){
      val path = baseDir + dir
      val folders = path.children(IsDirectory)
      val elems = sort(folders.toList.map(MyFile(_)))
      Json.toJson(elems)
    }
    Ok(json)
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
    val content = new java.io.File(baseDir + path)
    SimpleResult(
      header = ResponseHeader(OK, Map(
        CONTENT_LENGTH -> content.length.toString,
        CONTENT_TYPE -> play.api.libs.MimeTypes.forFileName(content.getName).getOrElse(play.api.http.ContentTypes.BINARY),
        CONTENT_DISPOSITION -> ("""attachment; filename="%s"""".format(content.getName))
        )),
      Enumerator.fromFile(content)
    )
    // Use this when PR617 will be merged (https://github.com/playframework/Play20/pull/617)
    //Ok.sendFile(new java.io.File(baseDir + path))
  }

  def open(path: String) = Action {
    val content = new java.io.File(baseDir + path)
    SimpleResult(
      header = ResponseHeader(OK, Map(
        CONTENT_LENGTH -> content.length.toString,
        CONTENT_TYPE -> play.api.libs.MimeTypes.forFileName(content.getName).getOrElse(play.api.http.ContentTypes.BINARY)
        )),
      Enumerator.fromFile(content)
    )
    // Use this when PR617 will be merged (https://github.com/playframework/Play20/pull/617)
    Ok.sendFile(content = new java.io.File(baseDir + path), inline = true)
  }

  def newFiles = Action { implicit request =>
    val json = Cache.getOrElse("top10", 60*5){
      val identity = Identity.get
      val paths = identity.get.folders.map(f => baseDir + '/' + f.path);
      val files = paths.flatMap(p => p ** ("*.{" + moviesExtensions.mkString(",") + "}"))
      val top10 = files.sortBy(_.lastModified).reverse.take(20)
      Json.toJson(top10.map(MyFile(_)))
    }
    Ok(json)
  }

  private def sort(list: List[MyFile])(implicit request: RequestHeader) = {
    val sortColumn = request.getQueryString("column").getOrElse("name")
    val sortOrder = request.getQueryString("order").getOrElse("asc") == "asc"
    val order = list.sortBy(file => sortColumn match {
      case "name" => file.name.toLowerCase
      case "season" => "%s%s".format(file.details.season, file.details.episode).trim
      case "quality" => file.details.quality
      case "size" => "%20d".format(file.rawSize)
      case "date" => "%20d".format(file.rawLastModified)
      case "year" => file.details.year
      case _ => file.name.toLowerCase
    })
    if (sortOrder) order else order.reverse
  }

  private def getCacheKey(dir: String, what: String)(implicit req: RequestHeader) = {
    Seq(what, dir, req.queryString.toString).mkString("-")
  }

}

case class MyFile(name: String, rawName: String, path: String, extension: String, lastModified: String, rawLastModified: Long,
    size: String, rawSize: Long, isFile: Boolean, details: MyFileDetails)
case class MyFileDetails(quality: String, lang: String, year: String, season: String, episode: String)

object MyFile {
  def apply(path: Path): MyFile = {
    MyFile(
        name      = getCleanName(path.name),
        rawName   = path.name,
        path      = path.relativize(Api.baseDir).path,
        extension = path.extension.getOrElse(""),
        lastModified = dateFormater.print(path.lastModified),
        rawLastModified = path.lastModified,
        size      = getSize(path.size).getOrElse(""),
        rawSize   = path.size.getOrElse(-1),
        isFile    = path.isFile,
        details = MyFileDetails(
          quality   = getQuality(path.name),
          lang      = getLang(path.name),
          year      = getYear(path.name).getOrElse(""),
          season    = getSeason(path.name).getOrElse(""),
          episode   = getEpisode(path.name).getOrElse("")
        )
     )
  }

  private lazy val conf = play.api.Play.current.configuration.getConfig("config").get
  private val qualities = conf.getStringList("formats").get.toSeq
  private val langs = conf.getStringList("langs").get.toSeq
  private val badKeywords = conf.getStringList("blacklist").get.toSeq

  private val dateFormater = DateTimeFormat.shortDateTime.withLocale(Locale.FRANCE)

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
   val tmp = name//.toLowerCase
       .replaceAll(Api.moviesExtensions.map("\\."+_).mkString("|"), "") // Remove extensions
       .replaceAll("""\[.*\]""", "") // Remove quality/lang
       .replaceAll("""\(.*\)""", "") // Remove year
       .replaceAll("""[_\.]""", " ") // Replace special chars by space
       .replaceAll(badKeywords.mkString(" ", "| ", ""), " ") // Remove blacklisted keywords
       .replaceAll("""[ ]{1,99}""", " ") // Remove unnecessary spaces
       .trim
   //tmp(0).toString.toUpperCase + tmp.drop(1) // Capitalize name
       tmp
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

object MyFileJson {

  

}
