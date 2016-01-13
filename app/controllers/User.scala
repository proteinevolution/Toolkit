package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by lukas on 1/13/16.
  */
class User extends Controller {


  def test = Action { implicit request =>

    models.Alnviz.inputForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest("this was an error")
      },
      contact => {

        print(contact)
        Ok
      }
    )
  }
}
