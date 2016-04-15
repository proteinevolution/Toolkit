package models.graph.nodes

import models.graph.CLU
import models.graph.Ports.{Alignment}

/**
  * Created by lzimmermann on 15.04.16.
  */
object PsiblastNode extends Node {

  val toolname = "psiblast"

  val inports = Vector(Alignment("alignment", CLU))
  val outports = Vector.empty
}
