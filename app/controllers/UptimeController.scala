package controllers

import play.api.mvc._

class UptimeController extends Controller {

  val startTime : Long = System.currentTimeMillis()

  def uptime = Action {
    val uptimeInMillis = System.currentTimeMillis() - startTime
    Ok(s"$uptimeInMillis ms")
  }
}