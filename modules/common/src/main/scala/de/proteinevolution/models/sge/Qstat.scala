package de.proteinevolution.models.sge

import sys.process._
import scala.language.postfixOps

/**
 * Model for qstat command to monitor the number of jobs in the SGE queue
 *
 */
final class Qstat {

  //private[this] val cmd = "qstat -u \"*\"" !!

  def get(): Unit = {}

}
