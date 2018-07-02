package services

import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

sealed trait Configuration

@Singleton
final class ConfigurationImpl @Inject()(
    appLifecycle: ApplicationLifecycle,
    ws: WSClient
) extends Configuration {

  private val logger = Logger(this.getClass)

  appLifecycle.addStopHook { () =>
    logger.info("configuring hostname .... ")
    ws.url("https://toolkit.tuebingen.mpg.de").execute("GET")
  }

}
