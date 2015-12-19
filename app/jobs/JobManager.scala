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

  private var jobID: Long = 0


  def job(exec: () => JobResult) : Long = {

    this.jobID += 1
    JobMap.put(this.jobID, Future {exec()})
    this.jobID
  }
}
