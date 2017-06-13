package services
import javax.inject._

import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.{ WSClient, WSResponse }

import scala.concurrent.Future

/**
  * Created by snam on 22.05.17.
  */
trait Configuration {

  def hello(): Unit
  def goodbye(): Unit

}

@Singleton
final class ConfigurationImpl @Inject()(appLifecycle: ApplicationLifecycle, ws: WSClient) extends Configuration {

  override def hello(): Unit = {

    println("configuring hostname .... ")
    ws.url("https://toolkit.tuebingen.mpg.de").get()

  }

  override def goodbye(): Unit = println("Goodbye!")

  def start(): Unit = hello()

  appLifecycle.addStopHook { () =>
    goodbye()
    Future.successful(())
  }

  start()

}
