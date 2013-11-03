package controllers

import scala.concurrent.ExecutionContext.Implicits._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import services.security.SecuredController
import services.{MovieDbNoResultException, MovieDB}

import models.moviedb.MovieDbDetail

object Movie extends Controller with SecuredController {

  def detail(name: String) = Secured { request =>
    Async {
      MovieDB.findMovieDetail(name).map { movie =>
        Ok(Json.toJson(movie))
      } recover {
        case _: MovieDbNoResultException => NotFound
      }
    }
  }
}