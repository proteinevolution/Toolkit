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
  // all jobs of registered users that are older than the given number are permanently deleted everywhere
  val deletionThresholdRegistered : Int = 90
  // all jobs of non registered users that are older than the given number are permanently deleted everywhere
  val deletionThreshold           : Int = 24
  // time in hours in which the job sweeping routine is executed
  val deletionCycle               : Int = 1

  // User Deletion settings
  val userDeletionDelay              : FiniteDuration = 10 minutes // Sweeps at this time after server start
  val userDeletionInterval           : FiniteDuration = 3 hours // Sweeps in this interval
  val userDeletingAfterMonths                   : Int = 1 // Deletes regular accounts after this timeframe
  val userAwaitingRegistrationDeletingAfterDays : Int = 3 // Deletes users awaiting registration after this timeframe (in days)
  val userLoggedInDeletingAfterMonths           : Int = 24 // Deletes registered accounts after this timeframe
  val userLoggedInWarningDaysBeforeDeletion     : Int = 14 // Sending an eMail to the user this many days prior to the deletion
}

trait ExitCodes {

  val SUCCESS    = 0
  val TERMINATED = 143
}
