package models.sge

import sys.process._
import scala.language.postfixOps

/**
  * Model for qstat command to monitor the number of jobs in the SGE queue
  * Created by snam on 24.03.17.
  */
final class Qstat {

  private[this] val cmd = "qstat -u \"*\"" !!

  def get() : Unit = {



  }

}
