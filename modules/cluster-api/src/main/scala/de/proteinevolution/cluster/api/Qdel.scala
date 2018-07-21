package de.proteinevolution.cluster.api

import sys.process._
import scala.language.postfixOps

object Qdel {

  def run(sgeJobId: String): Int = s"qdel $sgeJobId" !

}
