package models.job

import javax.inject.Singleton
import com.google.inject.ImplementedBy
import scala.util.Random

/**
  * Created by lzimmermann on 02.12.16.
  */
@ImplementedBy(classOf[JobIDProviderImpl])
sealed trait JobIDProvider {

  def provide: String
  def isAvailable(jobID: String): Boolean
}

@Singleton
class JobIDProviderImpl extends JobIDProvider {

  // TODO Ensure uniqueness of JobID, needs Database access (and a withFilter)
  private val jobIDStream = Stream.continually(Random.nextInt(9999999)).distinct.map(_.toString.padTo(7,'0')).iterator

  // TODO Prefill with database values
  private var usedJobIDs = Set.empty[String]

  def provide: String = {

    val res = jobIDStream.next()
    usedJobIDs = usedJobIDs + res
    res
  }

  def isAvailable(jobID : String): Boolean = ! usedJobIDs.contains(jobID)
}
