package controllers

import javax.inject.Inject

import actors._
import models.{Session, Alnviz}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.Logger


/**
  *
  * Created by lukas on 1/16/16.
  */
class Alnviz @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val UID = "uid"
  val toolname = "alnviz"


  // Input Form Definition of this tool
  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(Alnviz.apply)(Alnviz.unapply)
  )

  def ccToMap(cc: AnyRef) =

    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
    }



  def show = Action { implicit request =>

    val view = views.html.alnviz.form(inputForm)

    Ok(views.html.roughtemplate(view)).withSession {

      val uid = request.session.get(UID).getOrElse {

        Session.next.toString
      }
      UserManager() ! SubscribeUser(uid)

      Logger.info("Request from  UID" + uid)
      request.session + (UID -> uid)
    }
  }

  def submit = Action { implicit request =>

      inputForm.bindFromRequest.fold(

        formWithErrors => {
          BadRequest("this was an error")
        },
        formdata => {

          val uid =  request.session.get(UID).get

          Logger.info("Alnviz received formdata" + formdata + "from uid " + uid + "\n")

          // TODO Do we really need to cast formdata into a map?

          UserManager() ! TellUser(uid, UserJobStart(ccToMap(formdata), toolname))
          Ok
        })
    }
}
