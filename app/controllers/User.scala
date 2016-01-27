package controllers


import akka.util.Timeout
import scala.concurrent.duration._
import play.api.mvc._

/**
  * Created by lukas on 1/20/16.
  */
class User extends Controller {

  val UID = "uid"

  // If the result JobState is not available after 5 seconds
  implicit val timeout : Timeout = 5.seconds


  /*
  def result(jobID: Long) = Action.async { implicit request =>

    Logger.info("Method was: " + request.method + "\n")

    val uid = request.session.get(UID).get

      (UserManager() ? TellUser(uid, AskJob(jobID))).mapTo[Job].map { job =>

        Logger.info("View for Job with tool" + job.toolname + " requested")

        // Decide which view to render based on the job state
        val view : Html = job.state match {

          case models.Done =>

            // TODO Replace by reflection
            job.toolname match {

              case "alnviz" => views.html.alnviz.result.render(jobID, job, request)
            }
        }

        request.method match {

          // Post requests will just get the rendered tool view
          case "POST" => Ok(view)

          // get request will also embed into the whole page
          case "GET" => Ok(views.html.roughtemplate(view))

        }
      }

  } */
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
