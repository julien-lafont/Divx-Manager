package models.moviedb

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class MovieDbDetail(id: Long, imdb_id: String, original_title: String, title: String,
                         overview: Option[String], tagline: Option[String], status: String,
                         popularity: Double, vote_average: Double, vote_count: Long,
                         homepage: Option[String], backdrop_path: String, poster_path: String,
                         genres: List[MovieDbGenre])
case class MovieDbGenre(id: Long, name: String)

object MovieDbDetail {
  implicit val movieDbGenreReader = Json.reads[MovieDbGenre]
  implicit val movieDbDetailReader = Json.reads[MovieDbDetail]
}