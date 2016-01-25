package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.Logger
/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */
class UserActor(uid: String)   extends Actor with ActorLogging {

  // The websocket that is attached to the User
  var ws: ActorRef = null

  // The UserActor knows all Jobs that belong t him
  val userJobs = new collection.mutable.HashMap[Long, models.Job]()


  def receive = LoggingReceive {


    case AskJob(jobID: Long) =>

      sender ! userJobs.get(jobID).get


    case AttachWS(ws_new) =>

      this.ws = ws_new
      context watch ws
      Logger.info("WebSocket atached successfully\n")


    case  UserJobStart(spec, toolname) =>

      Logger.info("User with ID " + uid + " wants to start job\n ")
      JobManager() ! PrepWD(spec, toolname, uid)


      // Notifies the user about a Job Status change
    case UserJobStateChanged(job, jobID: Long) =>

      Logger.info("User Actor "  + uid + " received Job state change: " + job.state + '\n')


      // Update the Job state in the Job Table
      userJobs.put(jobID, job)

      // Only then we will tell the WebSocket and hence the User that the Job is actually finished
      ws ! UserJobStateChanged(job, jobID)


    case Terminated(ws_new) =>

      ws = null
  }





}
object UserActor {

  def props(uid: String) = Props(new UserActor(uid))
}
