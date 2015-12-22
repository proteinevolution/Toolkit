package jobs

import models.JobResult
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import models.JobMap

/**
 *
 * This controller is responsible for handling the execution of jobs
 *
 * TODO Dependency Injection of the Executor
 *
 * Created by lzimmermann on 19.12.15.
 */
object JobManager {

  // Keeps track of the Job ID
  private var jobID: Long = 0


  /**
   * Starts a simple Job which is only able to evaluate a Scala Function `exec`
   * For some tools like AlnViz, which only need to pass the arguments to the Frontend,
   * this is totally enough
   *
   * @param toolID
   * @param exec
   * @return
   */
  def job(toolID: String, exec: () => JobResult) : Long = {

    this.jobID += 1
    JobMap.put(this.jobID, toolID, Future {exec()})
    this.jobID
  }
}
