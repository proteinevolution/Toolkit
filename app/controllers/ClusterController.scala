package controllers

import javax.inject._

import de.proteinevolution.models.sge.Cluster
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import sys.process._

/**
 * This controller will be used later for user interaction with the ClusterMonitor
 *
 */
@Singleton
final class ClusterController @Inject()(ws: WSClient,
                                        configuration: Configuration,
                                        cluster: Cluster,
                                        cc: ControllerComponents)
    extends AbstractController(cc) {

  def getLoad: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(("qstat" #| "wc -l").!!.toDouble / 32))
  }
}
