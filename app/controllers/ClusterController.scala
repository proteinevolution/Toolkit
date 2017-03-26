package controllers

import javax.inject._

import models.sge.Cluster
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}



/**
  * This controller will be used later for user interaction with the ClusterMonitor
  *
  * Created by snam on 19.03.17.
  */

@Singleton
final class ClusterController @Inject()(ws: WSClient, configuration: Configuration, cluster : Cluster) extends Controller {

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)


  // initialize liveTable with a real value

  def getLoad : Action[AnyContent] = Action {implicit request =>

    val load = cluster.getLoad.loadEst

    Ok(Json.toJson(load.toString))

  }

}
