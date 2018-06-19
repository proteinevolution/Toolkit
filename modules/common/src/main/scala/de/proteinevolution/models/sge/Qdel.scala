package de.proteinevolution.models.sge

import sys.process._
import scala.language.postfixOps

final class Qdel {

  def delete(jobID: String): Int = {
    s"qdel $jobID" !
  }

}
