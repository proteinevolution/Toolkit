package controllers

import javax.inject.Inject

import play.api.mvc._

class UptimeController @Inject()(cc : ControllerComponents) extends AbstractController(cc) {

  val startTime : Long = System.currentTimeMillis()

  def uptime = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"$uptimeInMillis ms")
  }
}