package models.data

import play.api.data.{Mapping}
import play.api.data.Forms._

/**
  * Created by lukas on 2/15/16.
  */
object DataTypes {

  case class Alignment(val format : AlignmentFormat) extends Inport[String]("alignment", text)
  case object Sequences extends Inport[String]("sequences", text)  // Implicitly Multi FASTA
}
sealed trait AlignmentFormat
case object A3M extends AlignmentFormat
case object TreeCon extends AlignmentFormat
case object MEGA extends AlignmentFormat
case object GCG extends AlignmentFormat
case object PIR extends AlignmentFormat
case object A2M extends AlignmentFormat
case object FASTA extends AlignmentFormat
case object Stockholm extends AlignmentFormat
case object EMBL extends AlignmentFormat

abstract class Port


// Each Inport Type must declare a String representation and
abstract class Inport[A](val str: String, val pattern : Mapping[A]) extends Port
