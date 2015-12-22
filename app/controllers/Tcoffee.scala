package controllers

import javax.inject.Inject

import jobs.JobManager
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.Parameters

/**
 *
 *
 * Created by snam on 21.12.15.
 */
class Tcoffee @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  // Define the input form
  // MAKE SURE that the keys in the mapping match the arguments of the corresponding
  // case class
  val inputForm = Form(
    mapping(
      "sequences" -> text
    )(Parameters.TCoffee.apply)(Parameters.TCoffee.unapply)
  )

  // #####################################################################################

  // Semantics: If we GET-Request the /tcoffee page, we want to see the
  // form of the tool, we submit an empty Form which will be filled

  def form = Action {
    Ok(views.html.tcoffee.form(inputForm))
  }

  def submit = Action { implicit request =>
    inputForm.bindFromRequest.fold(

      formWithErrors => {
        BadRequest("This was an error")
      },

      tcoffeeParam => {

        // TODO give a dummy job to the Job Manager. Should be replaced with a reasonable invocation
        // TODO Need a better job abstraction
        // Obtain a new Job for this submission
        val jobid = JobManager.job("tcoffee", { () =>

          Thread.sleep(5000)
          null
        })
        // Please take me to the result View of this job
        Redirect(s"/results/$jobid")
      }
    )
  }
}