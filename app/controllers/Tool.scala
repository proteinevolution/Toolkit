package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import models.database.Session
import models.tools._
import play.api.cache._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global



@Singleton
class Tool @Inject()(val messagesApi      : MessagesApi,
@NamedCache("userCache") userCache        : CacheApi,
                     val reactiveMongoApi : ReactiveMongoApi,
            implicit val mat              : Materializer,
    @Named("jobManager") jobManager       : ActorRef) extends Controller with I18nSupport with UserSessions {

  implicit val timeout = Timeout(5.seconds)
  val userCollection = reactiveMongoApi.database.map(_.collection("jobs").as[BSONCollection](FailoverStrategy()))


  def submit(toolname: String, start : Boolean, jobID : Option[String]) = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>

    // Fetch the job ID from the submission, might be the empty string
    //val jobID = request.body.asFormUrlEncoded.get("jobid").head --- There won't be a job ID in the request

    // TODO replace with reflection to avoid the need to mention each tool explicitly here
    val form = toolname match {
      case "alnviz" => Some(Alnviz.inputForm)
      case "tcoffee" => Some(Tcoffee.inputForm)
      case "hmmer3" => Some(Hmmer3.inputForm)
      case "hhpred" => Some(HHpred.inputForm)
      case "hhblits" => Some(HHblits.inputForm)
      case "psiblast" => Some(Psiblast.inputForm)
      case "mafft" => Some(Mafft.inputForm)
      case "reformatb" => Some(Reformatb.inputForm) // cluster version of reformat
      case "clans" => Some(Clans.inputForm)
      case _ => None
    }

    if (form.isEmpty)
      NotFound

    else {
      val boundForm = form.get.bindFromRequest

      boundForm.fold(
        formWithErrors => {

          BadRequest("There was an error with the Form")
        },
        _ => jobManager ! Prepare(user, jobID, toolname, boundForm.data, start = start)
      )
      Ok.withSession(sessionCookie(request, user.sessionID.get))
    }
  }
  }
}


