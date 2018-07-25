import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import io.sentry.Sentry
import play.api.ApplicationLoader.Context
import play.api.ApplicationLoader
import play.api.inject.bind
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceApplicationLoader, GuiceableModule }
import play.api.libs.concurrent.MaterializerProvider

import scala.sys.process._

class ToolkitAppLoader extends GuiceApplicationLoader {

  private val materializerOverrides: Seq[GuiceableModule] = Seq(
    bind[Materializer].toProvider[MaterializerProvider]
  )

  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    GuiceApplicationLoader.defaultOverrides(context) ++ materializerOverrides
  }

  override def builder(context: Context): GuiceApplicationBuilder = {
    sys.env.get("TK_SENTRY_DSN").foreach { dsn =>
      val values = List(
        "release"     -> build.BuildInfo.version.toString,
        "environment" -> sys.env.getOrElse("TK_ENV", "undefined"),
        "serverName"  -> "hostname".!!
      )
      Sentry.init(dsn + "?" + Uri.Query(values: _*).toString())
    }
    super.builder(context)
  }

}
