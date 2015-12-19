package models

import scala.concurrent.Future


/**
 *
 *
 * Created by lzimmermann on 19.12.15.
 */
object JobMap {

  private val jobMap : collection.mutable.Map[Long, Future[JobResult]] =
                       collection.mutable.Map[Long, Future[JobResult]]()

  def put(jobID: Long, future: Future[JobResult]): Unit = {

        this.jobMap += (jobID -> future)
  }

  def get(jobID: Long): Option[Future[JobResult]]  = {

    this.jobMap.get(jobID)
  }

  def size: Int = this.jobMap.size
}
