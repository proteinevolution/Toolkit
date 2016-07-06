package models.graph.nodes

import models.graph.CLU
import models.graph.Ports.Alignment

/**
  * Created by zin on 06.07.16.
  */
object ClansNode extends Node {


  val toolname = "clans"
  val inports = Vector(Alignment("alignment", CLU))
  val outports = Vector.empty

}
