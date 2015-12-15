package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

/**
 * Controller of the Alnviz tool
 *
 * TODO The form stuff should really be generated from the model
 *
 * Created by lzimmermann on 14.12.15.
 */
object Alnviz extends Controller{

  // Defines the fields of the Forms as Scala structure\
  // Make sure that the case class parameter names and the keys in the map `inputForm` match
  case class InputData(sequence: String, format: String)

  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(InputData.apply)(InputData.unapply)
  )


  // Semantics: If we GET-Request the /alnviz page, we want to see the 
  // form of the tool
  def form = Action {
    Ok(views.html.alnviz.form(inputForm))
  }

  // binds the values from the form into the Request and submits.
  def submit = Action { implicit request =>
    Ok("Hi Submission")
  }
}