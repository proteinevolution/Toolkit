package models.graph.nodes

import models.graph.Port
import scala.collection.mutable.ArrayBuffer

/**
  * Created by lukas on 2/25/16.
  */
abstract class Node {

  val toolname : String

  val inports : Vector[Port]
  val outports : Vector[Port]

  val inlinks : ArrayBuffer[(Int, Int, Node)] = ArrayBuffer.empty
  val outlinks : ArrayBuffer[(Int, Int, Node)] = ArrayBuffer.empty

}
