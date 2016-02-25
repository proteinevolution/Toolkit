package models.graph.nodes

import models.graph.Ports.Alignment
import models.graph.CLU


/**
  *
  * Node for the tool  ''alnviz'' in the dependency graph the tool wants to have an Alignment
  * in the CLU format as inport and does not declare any other inports or outports
  *
  * Created by lukas on 2/24/16.
  */
object AlnvizNode extends Node {

  val toolname = "alnviz"

  val inports = Vector(Alignment(CLU))

  val outports = Vector.empty
}
