package filters

import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by snam on 21.12.15.
 */
class AddResponseHeader {
  def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    val result = f(rh)
    result.map(_.withHeaders("FOO" -> "bar"))
  }
}
