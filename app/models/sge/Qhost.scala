package models.sge

import modules.parsers.Ops.QhostP

import sys.process._
import scala.language.postfixOps

/**
  * Model for qhost command to monitor the cluster load
  * Created by snam on 19.03.17.
  */


final class Qhost {


  def get() : List[QhostP.Node] = {

    QhostP.fromString("qhost" !!)

  }

}
