package models.graph.nodes

import models.graph.Ports.{Alignment, Sequences}
import models.graph.CLU

/**
  * Created by lukas on 2/24/16.
  */
object MafftNode extends Node {

  val toolname = "mafft"

  val inports = Vector(Sequences("sequences"))
  val outports = Vector(Alignment("sequences.clustalw_aln", CLU))
}
