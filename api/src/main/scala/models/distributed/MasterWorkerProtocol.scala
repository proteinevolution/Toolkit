package models.distributed

/**
  * Created by lzimmermann on 30.03.16.
  */
object MasterWorkerProtocol {

  //case class RegisterWorker(workerId: String) // Currently Master spawns workers
  case class WorkerRequestsWork(workerId: String)
  case class WorkIsDone(workerId: String, workId: String)
  case class WorkFailed(workerId: String, workId: String)

  // Messages to Workers
  case object WorkIsReady
  case class Ack(id: String)
}