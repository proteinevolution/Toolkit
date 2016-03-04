package models.graph.nodes

import models.graph.Ports.{Alignment, Sequences}
import models.graph.CLU

import scala.collection.mutable.ArrayBuffer

/**
  * Created by lukas on 2/24/16.
  */
object TcoffeeNode extends Node {

  val toolname = "tcoffee"

  val inports = Vector(Sequences(ArrayBuffer("sequences")))
  val outports = Vector(Alignment(ArrayBuffer("alignment"), CLU))
}
