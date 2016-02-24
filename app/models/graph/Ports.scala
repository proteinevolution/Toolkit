package models.graph

import models.tools.{TcoffeeNode, AlnvizNode}
import play.api.data._
import play.api.data.Forms._
import utils.Exceptions.ToolnameNotDistinctException

/**
  * Created by lukas on 2/15/16.
  */
object Ports {

  private val nodes = Array(AlnvizNode, TcoffeeNode)

  // Code to compute the dependency graph between the tools
  // 1. Check that the toolnames are unique
  if(nodes.map(_.toolname).distinct.length != nodes.length) {
    throw ToolnameNotDistinctException("Error: Toolnames are not distinct. ")
  }


  // Collects all Nodes which have the respective port type as input



  // Collects all Nodes which have the respective port as output



  // An MSA as ToolPort.
  // TODO We might want to distinguish between different Alphabets
  case class Alignment(override val pid : PortTag, alignmentFormat : AlignmentFormat)
    extends InportWithFormat(pid, "alignment", text, alignmentFormat) with Outport

  // TODO We assume this to be implicitly multi FASTA
  case class Sequences(override val pid : PortTag) extends Inport(pid, "sequences", text)

}


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



/*
 * Port Hierarchy
 */
abstract class Port(val pid : PortTag)


// Each Inport Type must declare a String representation and a mapping to a form field
abstract class Inport[A](pid : PortTag, val str: String, val pattern : Mapping[A] ) extends Port(pid) {

  val map = str -> pattern

}

sealed trait Outport extends Port


// Port that comes along with a format specification
abstract class InportWithFormat[A](pid : PortTag, str : String, pattern :  Mapping[A], val format : Format)
  extends Inport(pid, str, pattern) {

  val formatMap = str + "_format" -> text
}


/*
Port properties
 */
case class PortTag(val portName : String, val fullName : String, val description : Option[String])