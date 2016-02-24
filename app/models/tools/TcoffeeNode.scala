package models.tools

import models.graph.{PortTag, CLU}
import models.graph.Ports.{Alignment, Sequences}
import models.graph.nodes.Node1_1

/**
  * Created by lukas on 2/24/16.
  */
object TcoffeeNode extends Node1_1[String, String] {

  val toolname = "tcoffee"

  val inport1 = Sequences(PortTag("sequences", "Sequences", None))
  val outport1 = Alignment(PortTag("alignment", "Alignment", None), CLU)
}
