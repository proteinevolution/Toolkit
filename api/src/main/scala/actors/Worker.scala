package actors

import scala.concurrent.duration._
import actors.Worker.WorkComplete
import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout}
import models.distributed.MasterWorkerProtocol.{Ack, WorkIsDone, WorkIsReady, WorkerRequestsWork}
import models.distributed.Work
import play.api.Logger

object Worker {

  def props(master : ActorRef, workerID : String) = Props(new Worker(master, workerID))

  // Sent by Work Executor
  case object WorkComplete
}


/*
jobDB    : models.database.Jobs,
jobRefDB : models.database.JobReference
*/

class Worker(val master : ActorRef, val workerID : String) extends Actor with ActorLogging {

  val workExecutor = context.actorOf(WorkExecutor.props())

  var currentWorkId: Option[String] = None

  def workID: String = currentWorkId match {
    case Some(workId) => workId
    case None => throw new IllegalStateException("Not working")
  }


  def receive = idle
  /*
   *   Different receive states, worker starts with being Idle
   */



  // Worker has nothing to do
  def idle: Receive = {


    // Worker got to know that work is ready and requests some from the Master
    case WorkIsReady =>
      Logger.info("Worker " + workerID + " has requested Work")

      master ! WorkerRequestsWork(workerID)

    //
    case work@Work(workId, userRequest, userJob) =>
      Logger.info("Worker " + workerID + " received work")
      currentWorkId = Some(workId)
      workExecutor ! work
      // Now the worker is working
      context.become(working)
  }

  // Worker is currenty busy with another task
  def working: Receive = {

    case WorkComplete =>
      Logger.info("Work is complete")

      master ! WorkIsDone(workerID, workID)

      // Worker will wait for 5 seconds until result was ack
      context.setReceiveTimeout(5.seconds)
      context.become(waitForWorkIsDoneAck)

    case _: Work => Logger.info("Yikes. Master told me to do work, while I'm working.")
  }

  def waitForWorkIsDoneAck : Receive = {

    case Ack(id) if id == workID =>

      master ! WorkerRequestsWork(workerID)
      context.setReceiveTimeout(Duration.Undefined)
      context.become(idle)

    case ReceiveTimeout =>
      log.info("No ack from master, retrying")

      master ! WorkIsDone(workerID, workID)
  }





}
  /*
  def receive = LoggingReceive {



    case WRead(userJob) =>

      val main_id_o = jobDB.getMainID(userJob.user_id, userJob.job_id)
      main_id_o match {

        case Some(main_id) =>

          sender() ! s"$jobPath$main_id$SEP$PARAM_DIR".toFile.list.map { file =>

            file.name -> file.contentAsString
          }.toMap

        case None =>
          userJob.changeState(Error)
      }





    case WConvert(parentUserJob, childUserJob, links) =>

      // Assemble all necessary file paths
      val parent_main_id = jobDB.getMainID(parentUserJob.user_id, parentUserJob.job_id).get
      val child_main_id = jobDB.getMainID(childUserJob.user_id, childUserJob.job_id).get

      val parentRootPath = s"$jobPath$parent_main_id$SEP"
      val childRootPath = s"$jobPath$child_main_id$SEP"

      // If the Job directory does not exist yet, make a new one
      subdirs.foreach { s => (childRootPath + s).toFile.createDirectories() }

      for(link <- links) {

        val outport = parentUserJob.tool.outports(link.out)
        val inport = childUserJob.tool.inports(link.in)

        val params : Option[ArrayBuffer[String]] = Ports.convert(outport, inport)

        // Assemble paths to respective files
        val outfile = s"${parentRootPath}results/${outport.filename}"
        val infile =  s"$childRootPath$SEP$PARAM_DIR$SEP${inport.filename}"

        // Decide whether conversion is needed
        params match  {

          // This is the same format, just copy over the file
          case None =>

            outfile.toFile.copyTo(infile.toFile)
            childUserJob.changeInFileState(inport.filename, Ready)

            // If this port has a format, we also need to write the format file
            inport match {

              case portWithFormat : PortWithFormat =>

                s"$childRootPath$SEP$PARAM_DIR$SEP${portWithFormat.formatFilename}".toFile.write(portWithFormat.format.paramName)
                childUserJob.changeInFileState(portWithFormat.formatFilename, Ready)
            }

          case Some(buffer) => throw NotImplementedException("Format conversion is currently not supported")
        }
      }

*/