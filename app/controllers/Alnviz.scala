package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models.AlnvizFormData

/**
 * Controller of the Alnviz tool
 *
 * Created by lzimmermann on 14.12.15.
 */
class Alnviz  @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  // Define the input form
  // MAKE SURE that the keys in the mapping match the arguments of the corresponding
  // case class
  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(AlnvizFormData.apply)(AlnvizFormData.unapply)
  )

  // #####################################################################################

  // Semantics: If we GET-Request the /alnviz page, we want to see the 
  // form of the tool, we submit an empty Form which will be filled
  def form = Action {
    Ok(views.html.alnviz.form(inputForm))
  }

  // binds the values from the form into the Request and submits. TODO
  def submit = Action { implicit request =>
    Ok("Hi Submission")
  }
}