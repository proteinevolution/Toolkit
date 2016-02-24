package models.data

import play.api.data._
import play.api.data.Forms._

/**
  * Created by lukas on 2/15/16.
  */
object Ports {


  // An alignment in parameterized by its format
  case class Alignment(val format : AlignmentFormat) extends Inport[String]("alignment", text)


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


abstract class Port

// Each Inport Type must declare a String representation and a mapping to a form field
abstract class Inport[A](val str: String, val pattern : Mapping[A]) extends Port

