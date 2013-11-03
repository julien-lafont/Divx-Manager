package services

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models.moviedb.MovieDbConfiguration

object MovieDBSpec extends Specification {

  val id = 81
  val name = "Nausicaä - De la vallée du vent"

  "MovieDB" should {
    "be initialized before first WS" in new WithApplication {
      MovieDB.configuration.images.base_url must not(beNull[String])
      MovieDB.configuration.images.base_url must not be empty
      MovieDB.configuration.images.poster_sizes must not be empty
      MovieDB.configuration.images.backdrop_sizes must not be empty
    }
  }

  "MovieDB search WS" should {
    "Find a movie with the exact name" in new WithApplication {
      val results = await(MovieDB.searchMovie("Nausicaä - De la vallée du vent")).results
      results.headOption must beSome
      results.head.id === id
      results.head.title === name
    }

    "Find movies with an approximate name" in new WithApplication {
      await(MovieDB.searchMovie("Nausicaä - De la vallée du vent")).total_results === 1
      await(MovieDB.searchMovie("Nausicaä de la vallée du vent")).total_results === 1
      await(MovieDB.searchMovie("nausicaä de la vallée du vent")).total_results === 1
      await(MovieDB.searchMovie("nausicaa vallee vent")).total_results === 1
    }

    "Find many movies for a too large name" in new WithApplication {
      await(MovieDB.searchMovie("007")).total_results must be_>=(1)
      await(MovieDB.searchMovie("Star wars")).total_results must be_>=(1)
    }

    "Find the best movie by keeping the first result" in new WithApplication {
      await(MovieDB.searchMovieBestResult(name)).id === id
    }
  }

  "MovieDB translations WS" should {
    "list all translations available for a movie" in new WithApplication {
      await(MovieDB.translationsMovie(id)) must not be empty
      await(MovieDB.translationsMovie(id)) must contain("fr")
    }
  }

  "MovieDB detail WS" should {
    "return the french description of a movie when available" in new WithApplication {
      val result = await(MovieDB.findMovieDetail(name))
      result.id === 81
      result.overview.get must contain("guerre")
      result.genres.map(_.name) must contain("Action")
    }

    "throw a `NoResultException` if the film is not found" in new WithApplication {
      await(MovieDB.findMovieDetail("Hey I'm an awesome movie")) must throwA[MovieDbNoResultException]
    }
  }
}
