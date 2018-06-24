package de.proteinevolution.cluster.controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import sys.process._

@Singleton
class ClusterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // currently not in use, meant to initialize view with real load value
  def getLoad: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(("qstat" #| "wc -l").!!.toDouble / 32))
  }

}
