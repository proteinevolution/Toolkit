package models

import scala.collection.mutable
import scala.concurrent.Future


/**
 * This Model is intended to keep a map of JobID (Long) to the Future Object holding the
 * Job Results. We will also map each JobID to the associated tool.
 *
 * Created by lzimmermann on 19.12.15.
 */
object JobMap {

  /**
   * Holds the mapping of JobID to the JobResults
   */
  private val jobMap : collection.mutable.Map[Long, Future[JobResult]] =
                       collection.mutable.HashMap[Long, Future[JobResult]]()
  private val toolMap: collection.mutable.Map[Long, String] =
                       collection.mutable.HashMap[Long, String]()
  /**
   * Inserts the Job into the JobMap and also adds a corresponding entry into the
   * ToolMap
   *
   * @param jobID the JobID of the Job in question
   * @param future The Future object which holds the job results
   */
  def put(jobID: Long, toolID: String, future: Future[JobResult]): Unit = {

        this.jobMap += (jobID -> future)
        this.toolMap += (jobID -> toolID)
  }


  def get(jobID: Long): Future[JobResult]  = {

    this.jobMap.get(jobID).get
  }

  /**
   * Returns the tool identifier of the tool to which this Job belongs/
   *
   * @param jobID: jobID of the Job whose tool is of interest
   * @return The tool identifier which belongs to the provided job.
   */
  def which(jobID: Long): String = {

    this.toolMap.get(jobID).get
  }

  def size: Int = this.jobMap.size
}
