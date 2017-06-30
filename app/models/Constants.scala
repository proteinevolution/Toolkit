package models

import java.io.File
import com.typesafe.config.ConfigFactory

/**
  *
  * Created by lzimmermann on 29.05.16.
  */
// TODO This should not be a trait, because it already implements all of its members.
// TODO Rather, this should be a singleton object which dependent class get injected
trait Constants {

  val SEPARATOR: String               = File.separator
  val jobPath                         = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val serializedParam                 = "sparam"
  val nJobActors                      = 100
  val formMultiValueSeparator: String = " "
  val modellerKey: String             = "MODELIRANJE"
  val maxJobNum: Int                  = 60 // max number of jobs that can be submitted from one ip within maxJobsWithin
  val maxJobsWithin: Int              = 1 // time in minutes within the max number of jobs is applied
  val maxJobNumDay: Int               = 500 // max number of jobs that can be submitted from one ip within maxJobsWithinDay
  val maxJobsWithinDay: Int           = 1 // time in day within the max number of jobs is applied for a day
  val deletionThresholdRegistered : Int    = 40 // all jobs of registered users that are older than the given number are permanently deleted everywhere
  val deletionThreshold           : Int    = 2 // all jobs of non registered users that are older than the given number are permanently deleted everywhere

}

trait ExitCodes {

  val SUCCESS    = 0
  val TERMINATED = 143
}
