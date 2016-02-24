package models.tools

import models.graph.{PortTag, CLU}
import models.graph.Ports.Alignment
import models.graph.nodes.Node1_0


/**
  *
  * Node for the tool  ''alnviz'' in the dependency graph the tool wants to have an Alignment
  * in the CLU format as inport and does not declare any other inports or outports
  *
  * Created by lukas on 2/24/16.
  */
object AlnvizNode extends Node1_0[String] {

  val toolname = "alnviz"

  val inport1 = Alignment(PortTag("alignment", "Alignment", None), CLU)
}
