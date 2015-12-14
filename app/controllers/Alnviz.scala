package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._


/**
 * Controller of the Alnviz tool
 *
 * Created by lzimmermann on 14.12.15.
 */
object Alnviz extends Controller{

  // Defines the fields of the Forms as Scala structure
  case class InputData(sequence: String, testInt: Int)

  val inputForm = Form(
    mapping(
      "sequence" -> text,
      "testInt" -> number
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