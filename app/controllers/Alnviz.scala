package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.AlnvizParam
import jobs.JobManager


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
    )(AlnvizParam.apply)(AlnvizParam.unapply)
  )

  // #####################################################################################

  // Semantics: If we GET-Request the /alnviz page, we want to see the 
  // form of the tool, we submit an empty Form which will be filled
  def form = Action {
    Ok(views.html.alnviz.form(inputForm))
  }


  // we want to bind the values submitted by the user and show redirect to another controller
  def submit = Action { implicit request =>
    inputForm.bindFromRequest.fold(

      formWithErrors => {
        BadRequest("This was an error")
      },

      alnvizParam => {

        // TODO give a dummy job to the Job Manager. Should be replaced with a reasonable invocation
        // Obtain a new Job for this submission
        val jobid = JobManager.job { () =>

            Thread.sleep(5000)
            null
        }
        // Please take me to the result View of this job
        Redirect(s"/results/$jobid")
      }
    )
  }
}