package actors


import akka.actor.{PoisonPill, ActorLogging, Actor}
import akka.event.LoggingReceive
import helpers.FileAccess
import language.postfixOps
import play.api.Play.current
import java.io.File

/**
  * Created by lukas on 1/10/16.
  */

class StorageWorker extends Actor with ActorLogging {


  def receive = LoggingReceive {

    case PrepWD(details, jobID, startJob) =>

      // Parse the Get String as Scala Map
      val res = details.split('&') map { str =>
        val pair = str.split('=')
        pair(0) -> pair(1)
      } toMap


      // Prepare the Working directory under path
      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"
      FileAccess.mkdir(path)

      for ((key , value) <- res) {

        FileAccess.mkfile(path + "/" + key, value)
      }

      // Start prepared Job eventually
      if(startJob) sender ! JobInit(jobID)

      self ! PoisonPill
  }
}

