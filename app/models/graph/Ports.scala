package models.graph

import models.graph.Converters.ReformatConverter
import models.graph.nodes.{TcoffeeNode, AlnvizNode}
import play.api.Logger

import scala.collection.mutable.ArrayBuffer

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

      val classname1 = node1.inports(i).getClass.getSimpleName
      val classname2 = node2.outports(j).getClass.getSimpleName

      // the ports are compatible if they come from the same class
      if(classname1 == classname2) {
        Logger.info("Wire " + classname1)

        node1.inlinks += ((i, j, node2, classname1))
        node2.outlinks += ((j, i, node1, classname2))
      }
    }
  }


  // An MSA as ToolPort.
  // TODO We might want to distinguish between different Alphabets
  case class Alignment(override val filename : String, alignmentFormat : AlignmentFormat)
    extends PortWithFormat(filename, alignmentFormat)

  // TODO We assume this to be implicitly multi FASTA
  case class Sequences(override val filename : String) extends Port(filename)


  // Returns script name and parametes for conversion
  object PortConverter {

    def convert(portA : Port, portB : Port) :  ArrayBuffer[String] = {

      (portA, portB) match {

        case t :  (Alignment, Alignment) =>

          ReformatConverter.convert(t._1.format.asInstanceOf[AlignmentFormat], t._2.format.asInstanceOf[AlignmentFormat])


        case _  => throw new RuntimeException("Sorry, you have not specified a converter for this case")
      }
    }
  }
}


/*
 * A Port must declare a set of files (as filename) which are either produces or consumed during tool execution
 */
abstract class Port(val filename : String)
/*
 *  Port that also declares a format specification. Will require an adapter to link the ports
 */
abstract class PortWithFormat(filename : String, val format : Format) extends Port(filename) {

  val formatFilename : String = filename + "_format"

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

