package controllers

import play.api._
import play.api.mvc._
import scalax.file.Path
import scalax.file.ImplicitConversions._
import scalax.file.PathSet
import Path._
import scalax.file.PathMatcher._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.collection.JavaConversions._
import scala.math._
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import services.security._
import play.api.libs.iteratee._
import play.api.cache.Cache
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import java.text.SimpleDateFormat
import java.util.Date
import java.net.URLDecoder

object Api extends Controller with SecuredController {

  val dateRss = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")

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
    (__ \ "isMovie").write[Boolean] and
    (__ \ "serieName").writeNullable[String] and
    (__ \ "folderType").write[String] and
    (__ \ "details").write[MyFileDetails]
  )(unlift(MyFile.unapply))

  val baseDir = "./links"
  val moviesExtensions = Seq("avi", "wmv", "mkv", "mp4", "mpg", "srt")
  val timeout = 60*5

  def list(dir: String, column: String, order: String) = Secured { implicit request =>
    val completeDir = "/" + URLDecoder.decode(dir, "UTF-8")
    if (!Identity.isFolderAuthorized(completeDir)) Unauthorized
    val json = Cache.getOrElse(getCacheKey(completeDir, "files"), timeout) {
      val folderType = Identity.get.flatMap(_.folders.filter(_.path == completeDir.split("/")(1)).headOption).map(_.contentType).get

      val files = folderType match {
        case Other => (baseDir + completeDir) **  ("*.*")
        case _ => (baseDir + completeDir) **  ("*.{" + moviesExtensions.mkString(",") + "}")
      }

      val elems = sort(files.toList.map(MyFile(_, folderType)), column, order)
      Json.toJson(elems)
    }
    Ok(json)
  }

  def listDirs(dir: String, column: String, order: String) = Secured { implicit request =>
    val completeDir = "/" + URLDecoder.decode(dir, "UTF-8")
    if (!Identity.isFolderAuthorized(completeDir)) Unauthorized
    val json = Cache.getOrElse(getCacheKey(completeDir, "dirs"), timeout) {
      val folders = (baseDir + completeDir).children(IsDirectory)
      val elems = sort(folders.toList.map(MyFile(_, Other)), column, order)
      Json.toJson(elems)
    }
    Ok(json)
  }

  def roots = Secured { implicit request =>
    val folders = request.user.folders.map(f => Map(
        "dir" -> f.path, 
        "name" -> f.name, 
        "type" -> f.contentType.toString()))
    Ok(Json.toJson(folders))
  }

  def rawListing(dir: String) = Secured { implicit request =>
    val completeDir = "/" + URLDecoder.decode(dir, "UTF-8")
    if (!Identity.isFolderAuthorized(completeDir)) Unauthorized
    val list = Cache.getOrElse(getCacheKey(completeDir, "listing"), timeout) {
      val files = (baseDir + completeDir) **  ("*.{" + moviesExtensions.mkString(",") + "}")
      val links = files.toList.map(f => routes.Api.download("/"+f.relativize(Api.baseDir).path).absoluteURL(true))
      links mkString "\n"
    }
    Ok(list)
  }

  def download(path: String) = Secured { implicit request =>
    if (!Identity.isFolderAuthorized(path)) Unauthorized
    Ok.sendFile(new java.io.File(baseDir + URLDecoder.decode(path, "UTF-8")))
  }

  def newFiles = Secured { implicit request =>
    val json = Cache.getOrElse("top10-" + request.user.name, timeout) {
      Json.toJson(getLastFiles(request.user, 25))
    }
    Ok(json)
  }

  def newFilesRSS = Secured { implicit request =>
    val rss = Cache.getOrElse("rss-" + request.user.name, 1) {
      wrapRSS(getLastFiles(request.user, 50).map { file =>
        <item>
          <title>{file.name}</title>
          <description>{file.rawName} ({file.size})</description>
          <link>{routes.Api.download("/"+file.path).absoluteURL(true)}</link>
          <pubDate>{dateRss.format(new Date(file.rawLastModified))}</pubDate>
        </item>
      })
    }
    Ok(rss)
  }

  private def wrapRSS(items : List[xml.Elem]) = {
    <rss version="2.0">
      <channel>
        <title>Divx Manager</title>
        <lastBuildDate>{dateRss.format(new Date())}</lastBuildDate>
        <link>https://pc.studio-dev.fr</link>
        <language>fr</language>
        {items}
      </channel>
    </rss>
  }

  private def getLastFiles(identity: Identity, nb: Int) = {
    val paths = identity.folders.map(f => baseDir + '/' + f.path);
    val files = paths.flatMap(p => p ** ("*.{" + moviesExtensions.mkString(",") + "}"))
    files.sortBy(_.lastModified).reverse.take(nb).map { file =>
      val path = file.path.drop(baseDir.length).split("/")(1)
      val folderType = identity.folders.filter(_.path == path).headOption.map(_.contentType).get
      MyFile(file, folderType)
    }
  }

  private def sort(list: List[MyFile], sortColumn: String, sortOrder: String)(implicit request: RequestHeader) = {
    val order = list.sortBy(file => sortColumn match {
      case "name" => file.name.toLowerCase
      case "season" => "%s%s".format(file.details.season, file.details.episode).trim
      case "quality" => file.details.quality
      case "size" => "%20d".format(file.rawSize)
      case "date" => "%20d".format(file.rawLastModified)
      case "year" => file.details.year
      case _ => file.name.toLowerCase
    })
    if (sortOrder == "asc") order else order.reverse
  }

  private def getCacheKey(dir: String, what: String)(implicit req: RequestHeader) = {
    Seq(what, dir, req.queryString.toString).mkString("-")
  }

}

case class MyFile(name: String, rawName: String, path: String, extension: String, lastModified: String, rawLastModified: Long,
    size: String, rawSize: Long, isFile: Boolean, isMovie: Boolean, serieName: Option[String], 
    folderType: String, details: MyFileDetails)
case class MyFileDetails(quality: String, lang: String, year: String, season: String, episode: String)

object MyFile {
  def apply(path: Path, folderType: FolderType): MyFile = {
    val serieName = folderType match {
      case TvShow => {
        val namePart = path.relativize(Api.baseDir).path.split("/")(1)
        Some(getCleanName(namePart))
      }
      case _ => None
    }

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
        isMovie   = path.extension.map(fileIsMovie).getOrElse(false),
        serieName = serieName,
        folderType = folderType.toString,
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
   val tmp = name
       .replaceAll(Api.moviesExtensions.map("\\."+_).mkString("|"), "") // Remove extensions
       .replaceAll("""\[.*\]""", "") // Remove quality/lang
       .replaceAll("""\(.*\)""", "") // Remove year
       .replaceAll("""[_\.]""", " ") // Replace special chars by space
       .replaceAll(badKeywords.mkString(" ", "| ", ""), " ") // Remove blacklisted keywords
       .replaceAll(badKeywords.mkString("-", "|-", ""), " ") // remove keywords with space
       .replaceAll("""[ ]{1,99}""", " ") // Remove unnecessary spaces
       .replaceAll("""s([\d]{1,2})e([\d]{1,2})""", "S$1E$2")
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

  private def fileIsMovie(extension: String) = {
    List("avi", "mkv", "mpg", "mp4", "mov", "wmv").contains(extension)
  }

}
