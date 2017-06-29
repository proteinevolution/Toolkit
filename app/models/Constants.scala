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
  val maxJobNum: Int                  = 100 // max number of jobs that can be submitted from one ip within maxJobsWithin
  val maxJobsWithin: Int              = 1 // time within the max number of jobs is applied
}

trait ExitCodes {

  val SUCCESS    = 0
  val TERMINATED = 143
}
