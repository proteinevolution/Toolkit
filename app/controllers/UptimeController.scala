package controllers

import javax.inject.Inject
import build.BuildInfo
import play.api.mvc._

class UptimeController @Inject()(cc: ControllerComponents)
    extends AbstractController(cc) {

  private val startTime: Long = System.currentTimeMillis()

  def uptime = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"$uptimeInMillis ms")
  }

  def buildInfo = Action {
    Ok(s"${BuildInfo.toString}")
  }

}
