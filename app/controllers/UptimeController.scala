package controllers

import javax.inject.Inject
import build.BuildInfo
import play.api.mvc._

class UptimeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  private val startTime: Long = System.currentTimeMillis()

  private val versionString = {
    new java.lang.StringBuilder()
      .append("Version: ")
      .append(BuildInfo.version)
      .append(" on Scala ")
      .append(BuildInfo.scalaVersion)
      .append(" with Sbt ")
      .append(BuildInfo.sbtVersion)
      .append(" and Play! ")
      .append(BuildInfo.playVersion)
      .toString
  }

  def uptime = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"$uptimeInMillis ms")
  }

  def buildInfo = Action {
    Ok(s"$versionString")
  }

}
