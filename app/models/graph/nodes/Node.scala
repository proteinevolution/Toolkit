package models.graph.nodes

import models.graph.{Outport, Inport}

/**
  * Created by lukas on 2/25/16.
  */
abstract class Node {

  val toolname : String

  val inports : Vector[Inport]
  val outports : Vector[Outport]
}
