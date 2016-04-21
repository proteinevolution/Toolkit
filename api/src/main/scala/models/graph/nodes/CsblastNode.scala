package models.graph.nodes

import models.graph.CLU
import models.graph.Ports.Alignment

/**
  * Created by lzimmermann on 21.04.16.
  */
object CsblastNode extends Node {

  val toolname = "csblast"

  val inports = Vector(Alignment("alignment", CLU))
  val outports = Vector.empty
}
