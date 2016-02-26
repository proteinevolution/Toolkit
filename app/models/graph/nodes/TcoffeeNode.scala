package models.graph.nodes

import models.graph.Ports.{Alignment, Sequences}
import models.graph.CLU

/**
  * Created by lukas on 2/24/16.
  */
object TcoffeeNode extends Node {

  val toolname = "tcoffee"

  val inports = Vector(Sequences(Array("sequences")))
  val outports = Vector(Alignment(Array("alignment"), CLU))
}
