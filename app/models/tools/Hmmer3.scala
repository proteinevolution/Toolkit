package models.tools

import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here

/**
  * Singleton object that stores general information about a tool
  */
object Hmmer3 extends ToolModel {

  val inport = null

  val toolname = "hmmer3"
  val fullName = "Hmmer3"

/*
  val inports  = Map(

    Sequences -> 1 // TCoffee needs one Set of Sequences
  )
*/

  //-----------------------------------------------------------------------------------------------

  // Input Form Definition of this tool
  val inputForm = Form(
    mapping(
      "sequences" -> text,
      "mlalign_id_pair" -> boolean,
      "mfast_pair" -> boolean,
      "mslow_pair" -> boolean
    )(Hmmer3.apply)(Hmmer3.unapply)
  )

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "sequences" -> "Sequences to be aligned")

  // TODO We need a better abstraction for the tool result names
  val resultFileNames = Vector("result")
}
case class Hmmer3(sequences: String, mlalign_id_pair: Boolean, mfast_pair : Boolean, mslow_pair : Boolean)