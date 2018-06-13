package services
import javax.inject._
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

sealed trait Configuration

@Singleton
final class ConfigurationImpl @Inject()(appLifecycle: ApplicationLifecycle, ws: WSClient)() extends Configuration {

  appLifecycle.addStopHook { () =>
    Logger.info("configuring hostname .... ")
    ws.url("https://toolkit.tuebingen.mpg.de").get().map(_ => ())
  }

}
