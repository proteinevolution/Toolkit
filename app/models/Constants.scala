package models

import java.io.File
import com.typesafe.config.ConfigFactory
import javax.inject.Singleton
import scala.concurrent.duration._

/**
  *
  * Created by lzimmermann on 29.05.16.
  */
@Singleton
class Constants {

  val SEPARATOR: String               = File.separator
  val jobPath                         = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val deletionLogPath: String         = s"${ConfigFactory.load().getString("deletion_log_path")}$SEPARATOR"
  val serializedParam                 = "sparam"
  val nJobActors                      = 100
  val formMultiValueSeparator: String = " "
  val modellerKey: String             = "MODELIRANJE"
  val breakAfterClustal: Int = 85 // clustal format breaks after n chars

  // Job limitation settings
  val maxJobNum: Int                  = 100 // max number of jobs that can be submitted from one ip within maxJobsWithin
  val maxJobsWithin: Int              = 1 // time in minutes within the max number of jobs is applied
  val maxJobNumDay: Int               = 4000 // max number of jobs that can be submitted from one ip within maxJobsWithinDay
  val maxJobsWithinDay: Int           = 1 // time in days within the max number of jobs is applied for a day

  // Job deletion settings
  /** Sweeps at this time after server start */
  val jobDeletionDelay         : FiniteDuration = 10 minutes
  /** Sweeps in this interval */
  val jobDeletionInterval      : FiniteDuration = 3 hours

  /** all jobs of registered users that are older than the given number are permanently deleted everywhere */
  val jobDeletionRegistered : Int = 90
  /** all jobs of non registered users that are older than the given number are permanently deleted everywhere */
  val jobDeletion           : Int = 24
  /** time in days in which the job should not have been viewed */
  val jobDeletionLastViewed : Int = 7

  // User Deletion settings
  /** Sweeps at this time after server start */
  val userDeletionDelay         : FiniteDuration = 70 minutes
  /** Sweeps in this interval */
  val userDeletionInterval      : FiniteDuration = 3 hours
  /** Sending an eMail to the user this many days before the deletion */
  val userDeletionWarning       : Int = 14 //days
  /** Deletes regular accounts after this time frame */
  val userDeleting              : Int =  1 //months
  /** Deletes users awaiting registration after this time frame */
  val userDeletingRegisterEmail : Int =  3 //days
  /** Deletes registered accounts after this time frame */
  val userDeletingRegistered    : Int = 24 //months
}

trait ExitCodes {

  val SUCCESS    = 0
  val TERMINATED = 143
}
