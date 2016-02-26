package models.graph

import models.graph.nodes.{TcoffeeNode, AlnvizNode}

/**
  * Created by lukas on 2/15/16.
  */
object Ports {


  val nodes = Vector(AlnvizNode, TcoffeeNode)

  val nodeMap = Map(

    AlnvizNode.toolname -> AlnvizNode,
    TcoffeeNode.toolname -> TcoffeeNode
  )


  for(node1 <- nodes; node2 <- nodes) {
    for(i <- node1.inports.indices; j <- node2.outports.indices ) {

      val x = node1.inports(i)
      val y = node2.outports(j)

      // the ports are compatible if they come from the same class
      if(x.getClass.getName == y.getClass.getName) {

        node1.inlinks += ((i, j, node2))
        node2.outlinks += ((j, i, node1))
      }
    }
  }



  // An MSA as ToolPort.
  // TODO We might want to distinguish between different Alphabets
  case class Alignment(override val files : Array[String], alignmentFormat : AlignmentFormat)
    extends PortWithFormat(files, alignmentFormat)

  // TODO We assume this to be implicitly multi FASTA
  case class Sequences(override val files : Array[String]) extends Port(files)
}


/*
 * A Port must declare a set of files (as filename) which are either produces or consumed during tool execution
 */
abstract class Port(val files : Array[String])


/*
 *  Port that also declares a format specification. Will require an adapter to link the ports
 */
abstract class PortWithFormat(files : Array[String], val format : Format) extends Port(files)



// Formats // TODO Deal with format conversion

abstract class Format(val fullName : String)

abstract class AlignmentFormat(fullName : String) extends Format(fullName)

case object CLU extends AlignmentFormat("CLUSTALW")
case object STO extends AlignmentFormat("Stockholm")
case object EMB extends AlignmentFormat("EMBL")
case object GBK extends AlignmentFormat("GBK")
case object MEG extends AlignmentFormat("MEGA")
case object MSF extends AlignmentFormat("GCG/MSF")
case object NEX extends AlignmentFormat("NEX")
case object PHY extends AlignmentFormat("PHY")
case object PIR extends AlignmentFormat("PIR/NBRF")
case object TRE extends AlignmentFormat("TREECON")













