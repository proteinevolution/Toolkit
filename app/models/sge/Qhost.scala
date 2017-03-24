package models.sge

import modules.parsers.Ops.QhostP

import sys.process._
import scala.language.postfixOps

/**
  * Model for qhost command to monitor the cluster load
  * Created by snam on 19.03.17.
  */


final class Qhost {


  private[this] val cmd = "qhost" !!

  def get() : List[QhostP.Node] = {

    val result = QhostP.fromString(cmd)

    /*for (x <- result) {
      println(x.hostname + " " + x.ncpu + " " + x.load + " " + x.memtot + " " + x.memuse)
    } */

    result

  }

}
