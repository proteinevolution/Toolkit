package models.graph.nodes

import models.graph.Ports.{Alignment, Sequences}
import models.graph.{CLU, PortTag}

/**
  * Created by lukas on 2/24/16.
  */
object TcoffeeNode extends Node {

  val toolname = "tcoffee"

  val inports = Vector(Sequences(PortTag("sequences", "Sequences", None)))


  val outports = Vector(Alignment(PortTag("alignment", "Alignment", None), CLU))
}
