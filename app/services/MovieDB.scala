package services

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

import play.api.Play.current
import play.api.libs.ws.WS

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger
import play.api.libs.ws.WS.WSRequestHolder

import models.moviedb._
import play.api.cache.Cache

object MovieDB extends MovieDBWs {

  // MovieDB Configuration
  lazy val configuration = Await.result(loadConfiguration(), Duration("10s"))

  /**
   * Search a movie
   */
  def searchMovie(query: String): Future[MovieDbSearchResults] = {
    fetch[MovieDbSearchResults] {
      ws("/search/movie")
        .withQueryString("query" -> query, "language" -> "fr")
    }
  }

  /**
   * Return the best mathching result or throw a MovieDbNoResultException if no result found
   */
  def searchMovieBestResult(query: String): Future[MovieDbSearchResult] = {
    searchMovie(query)
      .flatMap { searchResults =>
        searchResults.results.headOption match {
          case Some(result) => Future.successful(result)
          case None => Future.failed(new MovieDbNoResultException)
        }
    }
  }

  /**
   * Return all details on a movie, in the given language
   */
  def detailMovie(id: Long, language: String = "fr"): Future[MovieDbDetail] = {
    fetch[MovieDbDetail] {
      ws(s"/movie/$id")
        .withQueryString("language" -> language)
    }
  }

  /**
   * Return all available translations for a given movie
   */
  def translationsMovie(id: Long): Future[Seq[String]] = {
    val translationReader = Reads { json => JsSuccess((json \\ "iso_639_1").map(_.as[String])) }
    fetch {
      ws(s"/movie/$id/translations")
    }(translationReader)
  }

  /**
   * Search a movie with the best translation as possible
   */
  def findMovieDetail(name: String): Future[MovieDbDetail] = {
    for {
      result <- searchMovieBestResult(name)
      translations <- translationsMovie(result.id)
      detail <- detailMovie(result.id, translations.find(_ == "fr").getOrElse("en"))
    } yield detail
  }

  /**
   * Load movieDB configuration (contains baseUrl, image sizes...)
   */
  def loadConfiguration(): Future[MovieDbConfiguration] = {
    fetch[MovieDbConfiguration](ws("/configuration"))
  }

}

trait MovieDBWs {

  private lazy val key = current.configuration.getString("themoviedb.key").get
  private lazy val baseurl = current.configuration.getString("themoviedb.url").get
  private val timeout = 60 * 60 * 24

  protected def ws(url: String) = WS.url(s"$baseurl$url").withQueryString("api_key" -> key)

  protected def fetch[T](ws: WSRequestHolder)(implicit reader: Reads[T]): Future[T] = {
    val url = s"${ws.url}?${ws.queryString.map(q => q._1 + "=" + q._2.head).mkString("&")}"

    withCache(url) {
      Logger.trace(s">>> $url")
      ws.get.flatMap(processResponse).recoverWith {
        // If rate limit exceeded, retry in 10s *EXPERIMENTAL*
        case _: MovieDbRateLimitException => Future(Thread.sleep(10 * 1000)).flatMap(_ => ws.get.flatMap(processResponse))
        case other => Future.failed(other)
      }.flatMap(json => parse(json)(reader))
    }
  }

  protected def processResponse(r: play.api.libs.ws.Response) = {
    if (r.status == 503) {
      Future.failed(new MovieDbRateLimitException)
    } else if (r.status != 200) {
      Future.failed(new MovieDbWsException(s"Invalide response received: ${r.status} : ${r.body}"))
    } else {
      val remaining = r.header("X-Apiary-Ratelimit-Remaining").getOrElse("1000").toInt
      if (remaining < 10) Logger.warn(s"Rate limit warning: $remaining left!" )
      Logger.trace("<<<" + r.body)
      Future.successful(r.json)
    }
  }

  protected def parse[T](body: JsValue)(implicit reader: Reads[T]): Future[T] = {
    body.validate[T](reader) match {
      case JsSuccess(e, _) => Future.successful(e)
      case e: JsError => {
        Logger.error(JsError.toFlatJson(e).toString)
        Future.failed(new MovieDbJsonException(e))
      }
    }
  }

  private def withCache[A](key: String)(block: => A)(implicit ct : scala.reflect.ClassTag[A]) = {
    Cache.getOrElse(key, timeout)(block)
  }

}

sealed case class MovieDbWsException(message: String = "") extends RuntimeException(message)
sealed class MovieDbJsonException(val jsError: JsError) extends MovieDbWsException("Unable to parse Json")
sealed class MovieDbRateLimitException extends MovieDbWsException("Rate limit exceeded")
sealed class MovieDbNoResultException extends MovieDbWsException("No result found for this request")