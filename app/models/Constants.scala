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

  val SEPARATOR: String                    = File.separator
  val jobPath                              = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val serializedParam                      = "sparam"
  val nJobActors                           = 100
  val formMultiValueSeparator     : String = " "
  val modellerKey                 : String = "MODELIRANJE"
  val deletionThresholdRegistered : Int    = 140 // all jobs of registered users that are older than the given number are permanently deleted everywhere
  val deletionThreshold           : Int    = 139 // all jobs of non registered users that are older than the given number are permanently deleted everywhere
}

trait ExitCodes {

  val SUCCESS    = 0
  val TERMINATED = 143
}
