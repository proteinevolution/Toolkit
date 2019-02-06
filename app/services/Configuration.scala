package services

import javax.inject._
import play.Environment
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

sealed trait Configuration

@Singleton
final class ConfigurationImpl @Inject()(
    appLifecycle: ApplicationLifecycle,
    ws: WSClient,
    environment: Environment
) extends Configuration
    with Logging {

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
