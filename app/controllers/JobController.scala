package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor
import actors.JobActor.RunscriptData
import actors.Master.CreateJob
import akka.actor.{ActorRef, ActorSystem, Props}
import models.Values
import models.database.{JobState, Jobitem, Submitted}
import models.job.JobIDProvider
import models.tools.ToolModel
import models.tools.ToolModel.Toolitem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import play.twirl.api.Html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
class JobController @Inject() (jobIDProvider: JobIDProvider,
                               actorSystem : ActorSystem,
                               jobActorFactory : JobActor.Factory,
                               implicit val userCache : CacheApi,
                               final val values : Values,
                               @Named("master") master: ActorRef,
                               @NamedCache("jobitem") jobitemCache : CacheApi,
                               @NamedCache("jobActorCache") val jobActorCache: CacheApi,
                               val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with UserSessions {

  // Job Controller can directly send message to the JobActor if Possible, otherwise
  // the Master Actor needs to be used
  // TODO We might introduce more Masters and split them up on jobID ranges
  /**
    * Action requests a new Job instance at the Master.
    *
    */






  def create(toolname: String, jobIDoption : Option[String]):Action[AnyContent] = Action.async { implicit request =>

    // Determine whether the user Provided jobID is valid
    if (jobIDoption.isDefined && !jobIDProvider.isAvailable(jobIDoption.get)) {

      Future.successful(BadRequest)
    } else {

      val jobID = jobIDoption.getOrElse(jobIDProvider.provide)
      val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)

      getUser.map { user =>

        master ! CreateJob(jobID, Left(user.userID.stringify), RunscriptData(toolname, formData))

        val date = DateTime.now

        // TODO Use toolitem cache here
        val toolitem = ToolModel.toolMap(toolname).toolitem(values)

        // Establish a suitable JobItem in the Cache for the getJobAction
        jobitemCache.set(jobID, Jobitem(jobID, jobID, jobID, Submitted, user.userID.stringify,
          DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(date), toolitem, Seq.empty, formData))

        // Job Accepted, provide jobID:
        Ok(Json.obj("jobID" -> jobID))
      }
    }
  }



  def get(jobID: String) = Action {




    Ok
  }



  /*
  context.actorOf(props(Props(create)), name)

   */

  def delete(jobID : String) = Action {

    Ok
  }




}
