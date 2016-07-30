package models.graph.nodes

import models.graph.Ports.{Alignment, Sequences}
import models.graph.CLU


object ReformatbNode extends Node {

  val toolname = "reformatb"

  val inports = Vector(Sequences("sequences"))
  val outports = Vector(Alignment("sequences.clustalw_aln", CLU))
}
