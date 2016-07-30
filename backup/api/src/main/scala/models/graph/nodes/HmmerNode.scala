package models.graph.nodes

import models.graph.CLU
import models.graph.Ports.Alignment

/**
  * Created by lukas on 3/11/16.
  */
object HmmerNode extends Node {

  val toolname = "hmmer3"
  val inports = Vector(Alignment("alignment", CLU))
  val outports = Vector.empty
}
