import controllers.AssetsFinder
import de.proteinevolution.base.helpers.ToolkitTypes
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.{ Inject, Singleton }

@Singleton
final class ErrorHandler @Inject()(assets: AssetsFinder) extends HttpErrorHandler with ToolkitTypes {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    fuccess(Status(statusCode)(views.html.errors.pagenotfound(assets)))
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    fuccess(InternalServerError(exception.toString))
  }
}
