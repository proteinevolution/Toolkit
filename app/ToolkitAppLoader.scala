import akka.stream.Materializer
import de.proteinevolution.migrations.services.MongobeeRunner
import play.api.ApplicationLoader
import play.api.ApplicationLoader.Context
import play.api.inject.bind
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceApplicationLoader, GuiceableModule }
import play.api.libs.concurrent.MaterializerProvider

import com.typesafe.config.ConfigFactory

class ToolkitAppLoader extends GuiceApplicationLoader {

  private val materializerOverrides: Seq[GuiceableModule] = Seq(
    bind[Materializer].toProvider[MaterializerProvider]
  )

  private val config = ConfigFactory.load()

  private val mongoUri = config.getString("mongodb.uri")

  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    GuiceApplicationLoader.defaultOverrides(context) ++ materializerOverrides
  }

  override def builder(context: Context): GuiceApplicationBuilder = {
    new MongobeeRunner(mongoUri).run()
    super.builder(context)
  }

}
