package actors
import akka.actor.{Props, Actor, FSM}
import models.database._
import scala.concurrent.duration._


/**
 *
 * Created by snam on 11.11.16.
 */



object JobStateMonitor {

  def props(job : Job) = Props(new JobStateMonitor(job))


  trait Factory {
    def apply(key: String): Actor
  }
}


// Problem is that we need either database connection or actorref

class JobStateMonitor(job: Job) extends Actor with FSM[JobState, JobState] {

 // startWith(Submitted, job.status)

  //when(Submitted) {

    //case _ => goto(Prepared) forMax 5.seconds replying Submitted
    //case _ => goto(Error) replying Error

  //}



}


