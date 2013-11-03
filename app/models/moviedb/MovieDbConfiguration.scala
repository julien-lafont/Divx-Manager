package models.moviedb

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class MovieDbConfiguration(images: MovieDbConfigurationImage)
case class MovieDbConfigurationImage(base_url: String, secure_base_url: String, poster_sizes: Seq[String], backdrop_sizes: Seq[String])

object MovieDbConfiguration {
  implicit val movieDbConfigurationImageReader = Json.reads[MovieDbConfigurationImage]
  implicit val movieDbConfigurationReader = Json.reads[MovieDbConfiguration]
}