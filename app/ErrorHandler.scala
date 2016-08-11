/**
  * Created by zin on 17.07.16.
  */


import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import javax.inject.Singleton

@Singleton
class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)(views.html.errors.pagenotfound())
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {

    Future.successful(
      InternalServerError(exception.toString)
    )
  }
}