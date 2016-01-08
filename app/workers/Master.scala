package workers

import akka.actor.{Actor, Props}

/**
 * The idea is that each Job is associated with a Master worker.
 *
 * Created by lzimmermann on 22.12.15.
 */
object Master {
  def props = Props[Master]

  // Messages from Worker // TODO Define the Results of the Worker
  case class WorkIsDone(workerId: Int, workId: Int, result: Any)
  case class WorkFailed(workerId: Int, workId: Int)

  // Messages from Job Control
  case class WorkInit()

}

class Master extends Actor {
  import Master._

  /**
   * Master has to react to the messages from the workers
   * @return
   */
  def receive = {
    case WorkIsDone(workerId: Int, workId: Int, result: Any) =>
      sender() ! "Hello, " + workerId
  }
}

/* Akka conventions

    The messages it sends/receives, or its protocol, are defined on its companion object
    It also defines a props method on its companion object that returns the props for creating it

 */
