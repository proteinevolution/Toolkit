package services
import javax.inject._

import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

import scala.concurrent.Future

sealed trait Configuration {

  def hello(): Unit
  def goodbye(): Unit

}

@Singleton
final class ConfigurationImpl @Inject()(appLifecycle: ApplicationLifecycle, ws: WSClient)() extends Configuration {

  override def hello(): Unit = {
    println("configuring hostname .... ")
    val _ = ws.url("https://toolkit.tuebingen.mpg.de").get()
  }

  override def goodbye(): Unit = println("Goodbye!")

  def start(): Unit = hello()

  appLifecycle.addStopHook { () =>
    goodbye()
    Future.successful(())
  }

  start()

}
