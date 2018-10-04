package services

import javax.inject._
import play.Environment
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

sealed trait Configuration

@Singleton
final class ConfigurationImpl @Inject()(
    appLifecycle: ApplicationLifecycle,
    ws: WSClient,
    environment: Environment
) extends Configuration {

  private val logger = Logger(this.getClass)

  private def init(): Unit = {
    if (environment.isProd) {
      appLifecycle.addStopHook { () =>
        logger.info("configuring hostname .... ")
        ws.url("https://toolkit.tuebingen.mpg.de").execute("GET")
      }
    }
  }

  init()

}
