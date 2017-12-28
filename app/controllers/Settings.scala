package controllers

import play.api.mvc._
import javax.inject.{ Inject, Singleton }

import scala.sys.process._

@Singleton
final class Settings @Inject()(cc: ControllerComponents)() extends AbstractController(cc) {

  private[this] var cm = ""

  def clusterMode: String = {
    val hostname_cmd = "hostname"
    val hostname     = hostname_cmd.!!.dropRight(1)
    if (hostname.equals("olt") || hostname.equals("rye"))
      cm = "sge"
    else
      cm = "LOCAL"
    cm
  }
}
