package models.moviedb

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class MovieDbSearchResults(total_results: Int, results: List[MovieDbSearchResult])
case class MovieDbSearchResult(id: Long, popularity: Double, title: String, vote_average: Double, vote_count: Long)

object MovieDbSearchResult {
  implicit val movieDbSearchResultReader = Json.reads[MovieDbSearchResult]
}

object MovieDbSearchResults {
  implicit val movieDbSearchResultsReader = Json.reads[MovieDbSearchResults]
}
