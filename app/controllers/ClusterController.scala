package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import sys.process._

@Singleton
final class ClusterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getLoad: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(("qstat" #| "wc -l").!!.toDouble / 32))
  }

}
