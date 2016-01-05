package controllers

import javax.inject.Inject

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}



/**
  * Handels the form generation and submission of tools
  *
  * Created by lukas on 1/5/16.
  */

class Tool @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {


  /**
    * The user has requested to view a tool, so we present the toolview here
    *
     * @param toolname
    *  @return
    */
  def show(toolname: String) = Action { implicit request =>

    // Fetch the input form definition from the tool
    val inputForm = models.Values.modelMap.get(toolname).get.inputForm

    // Pass the input form to the form view of the  tool
    Ok(models.Values.viewMap.get(toolname).get(inputForm))
  }

  def submit(toolname: String) = Action { implicit request =>

    models.Values.modelMap.get(toolname).get.inputForm.bindFromRequest.fold(

      formWithErrors => {

        BadRequest("This was an error")
      },

     // TODO assemble new job from parameters
      parameters => {

        // tell JobManager that we have just submitted a Job with parameters

        val jobid = 0



        // Please take me to the result View of this job
        Redirect(s"/results/$jobid")
  })
}
}
