package models.graph.nodes

import models.graph.Inport

/**
  * Created by lukas on 2/24/16.
  */
abstract class Node1_1[A1, B1] extends NodeBase {

  val inport1 : Inport[A1]


  val outport1 : Inport[B1]

}



