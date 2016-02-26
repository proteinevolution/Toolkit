package models.graph

import models.graph.nodes.{TcoffeeNode, AlnvizNode}

/**
  * Created by lukas on 2/15/16.
  */
object Ports {


  val nodes = Vector(AlnvizNode, TcoffeeNode)


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
  case class Alignment(alignmentFormat : AlignmentFormat)
    extends InportWithFormat(alignmentFormat) with Outport

  // TODO We assume this to be implicitly multi FASTA
  case object Sequences extends Inport
}


/*
 * Port Hierarchy
 */
abstract class Port


// Each Inport Type must declare a String representation and a mapping to a form field
abstract class Inport extends Port
sealed trait Outport extends Port

// Port that comes along with a format specification
abstract class InportWithFormat(val format : Format) extends Inport



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













