/**
 * Created by snam on 21.12.15.
 */
import play.api._
import play.api.mvc._
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter(shouldGzip = (request, response) =>
  response.headers.get("Content-Type").exists(_.startsWith("text/html")))) with GlobalSettings {
  // onStart, onStop etc...
}