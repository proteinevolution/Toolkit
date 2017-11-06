package de.proteinevolution.models.sge

import de.proteinevolution.parsers.Ops.QhostP

import sys.process._
import scala.language.postfixOps

/**
 * Model for qhost command to monitor the cluster load
 *
 */

final class Qhost {

  def get(): List[QhostP.Node] = {

    QhostP.fromString("qhost" !!)

  }

}
