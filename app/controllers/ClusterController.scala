package controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This controller will be used later for user interaction with the ClusterMonitor
  * e.g. showing non-standard information for own cluster jobs
  * Created by snam on 19.03.17.
  */

@Singleton
final class ClusterController @Inject()(ws: WSClient, configuration: Configuration) extends Controller {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)


  def foo() : Action[AnyContent] = Action { implicit request =>

   Ok

  }

}
