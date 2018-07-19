import controllers.AssetsFinder
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.{ Inject, Singleton }

@Singleton
final class ErrorHandler @Inject()(assets: AssetsFinder)
    extends HttpErrorHandler {

  def onClientError(
      request: RequestHeader,
      statusCode: Int,
      message: String
  ): Future[Result] = {
    Future.successful(
      Status(statusCode)(views.html.errors.pagenotfound(assets))
    )
  }

  def onServerError(
      request: RequestHeader,
      exception: Throwable
  ): Future[Result] = {
    Future.successful(
      InternalServerError(exception.toString)
    )
  }
}
