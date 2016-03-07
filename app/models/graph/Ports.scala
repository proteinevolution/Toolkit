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




  def convert(portA : Port, portB : Port) :  Option[ArrayBuffer[String]] = {

      (portA, portB) match {

        case t :  (Alignment, Alignment) =>

          // Decide whether the Alignment needs to be converted
          if(t._1.format == t._2.format) None else
          Some(ReformatConverter.convert(t._1.format.asInstanceOf[AlignmentFormat], t._2.format.asInstanceOf[AlignmentFormat]))


        case _  => throw new RuntimeException("Sorry, you have not specified a converter for this case")
      }
    }
}


/*
 * A Port must declare a file via name which is either produces or consumed from the tool
 */
abstract class Port(val filename : String)
/*
 *  Port that also declares a format specification. Will require an adapter to link the ports
 */
abstract class PortWithFormat(filename : String, val format : Format) extends Port(filename) {

  val formatFilename : String = filename + "_format"
}



abstract class Format(val paramName : String)

abstract class AlignmentFormat(paramName : String) extends Format(paramName)

case object FAS extends AlignmentFormat("fas")
case object CLU extends AlignmentFormat("clu")
case object STO extends AlignmentFormat("sto")
case object EMB extends AlignmentFormat("emb")
case object GBK extends AlignmentFormat("gbk")
case object MEG extends AlignmentFormat("meg")
case object MSF extends AlignmentFormat("msf")
case object NEX extends AlignmentFormat("nex")
case object PHY extends AlignmentFormat("phy")
case object PIR extends AlignmentFormat("pir")
case object TRE extends AlignmentFormat("tre")
