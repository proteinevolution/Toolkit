package models.sge

import sys.process._
import scala.language.postfixOps

/**
  * Model for qhost command to monitor the cluster load
  * Created by snam on 19.03.17.
  */


class Qhost {

  case class Node(hostname: String, ncpu: Int, load: Double, memtot: Double, memuse: Double, swapto: Double, swapus: Double)



  private[this] val cmd = "qhost" !

  def qhost() = cmd



}
