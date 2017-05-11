package models.sge

import sys.process._
import scala.language.postfixOps

/**
  * Created by snam on 26.03.17.
  */
final class Qdel {

  def delete(jobID: String): Int = {

    s"qdel $jobID" !

  }

}
