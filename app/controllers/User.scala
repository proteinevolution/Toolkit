package controllers

import actors.{AskJob, TellUser, UserManager}
import akka.util.Timeout
import models.Job
import scala.concurrent.duration._
import akka.pattern.ask
import play.api.mvc._
import play.api.{Play, Logger}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lukas on 1/20/16.
  */
class User extends Controller {

  val UID = "uid"

  // If the result JobState is not available after 5 seconds
  implicit val timeout : Timeout = 5.seconds


  def result(jobID: Long) = Action.async { implicit request =>



    val uid = request.session.get(UID).get

      (UserManager() ? TellUser(uid, AskJob(jobID))).mapTo[Job].map { job =>

        Logger.info("View for Job with tool" + job.toolname + " requested")


        job.state match {

          case models.Done =>

            // TODO Replace by reflection
            job.toolname match {

              case "alnviz" => Ok(views.html.alnviz.result(jobID, job))
            }

          case models.Running => Ok(views.html.running(jobID))
        }
      }
  }
}


/*


scala> class Foo { def bar(x: Int) = x }
defined class Foo

scala> val foo = new Foo
foo @ 5935b50c: Foo = Foo@5935b50c

scala> runtimeMirror(getClass.getClassLoader).reflect(foo)
res0 @ 65c24701: reflect.runtime.universe.InstanceMirror = scala.reflect.runtime.JavaMirrors$JavaMirror$JavaInstanceMirror@65c24701

scala> res0.symbol.typeSignature.member(newTermName("bar"))
res1 @ 69624a1c: reflect.runtime.universe.Symbol = method bar

scala> res0.reflectMethod(res1.asMethodSymbol)(42)
res2 @ 4ac1d188: Any = 42

  *
  */
