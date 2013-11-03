package models.moviedb

import play.api.libs.json._
import play.api.libs.functional.syntax._
import services.MovieDB
import org.joda.time.LocalDate

case class MovieDbDetail(id: Long, imdb_id: String, original_title: String, title: String,
                         overview: Option[String], tagline: Option[String], status: String,
                         popularity: Double, vote_average: Double, vote_count: Long,
                         homepage: Option[String], backdrop_path: Option[String], poster_path: Option[String],
                         genres: List[MovieDbGenre], release_date: LocalDate)
case class MovieDbGenre(id: Long, name: String)

object MovieDbDetail {
  implicit val movieDbGenreReader = Json.format[MovieDbGenre]
  implicit val movieDbDetailReader = Json.reads[MovieDbDetail]

  private val imgBaseUrl = MovieDB.configuration.images.secure_base_url

  implicit val movieDbDetailWriter = (
    (__ \ "id").write[Long] and
    (__ \ "imdb_id").write[String] and
    (__ \ "original_title").write[String] and
    (__ \ "title").write[String] and
    (__ \ "overview").writeNullable[String] and
    (__ \ "tagline").writeNullable[String] and
    (__ \ "status").write[String] and
    (__ \ "popularity").write[String] and
    (__ \ "vote_average").write[Double] and
    (__ \ "vote_count").write[Long] and
    (__ \ "homepage").writeNullable[String] and
    (__ \ "poster_s").writeNullable[String] and
    (__ \ "poster_m").writeNullable[String] and
    (__ \ "poster_l").writeNullable[String] and
    (__ \ "poster_xl").writeNullable[String] and
    (__ \ "backdrop_s").writeNullable[String] and
    (__ \ "backdrop_m").writeNullable[String] and
    (__ \ "backdrop_l").writeNullable[String] and
    (__ \ "backdrop_xl").writeNullable[String] and
    (__ \ "genres").write[List[MovieDbGenre]] and
    (__ \ "release_date").write[String]
  )((movie: MovieDbDetail) => (
    movie.id,
    movie.imdb_id,
    movie.original_title,
    movie.title,
    movie.overview,
    movie.tagline,
    movie.status,
    movie.popularity.toString.take(4),
    movie.vote_average,
    movie.vote_count,
    movie.homepage,
    movie.poster_path.map(p => s"${imgBaseUrl}w154$p"),
    movie.poster_path.map(p => s"${imgBaseUrl}w342$p"),
    movie.poster_path.map(p => s"${imgBaseUrl}w500$p"),
    movie.poster_path.map(p => s"${imgBaseUrl}original$p"),
    movie.backdrop_path.map(p => s"${imgBaseUrl}w154$p"),
    movie.backdrop_path.map(p => s"${imgBaseUrl}w342$p"),
    movie.backdrop_path.map(p => s"${imgBaseUrl}w500$p"),
    movie.backdrop_path.map(p => s"${imgBaseUrl}original$p"),
    movie.genres,
    movie.release_date.toString("MMMM YYYY")
    ))
}