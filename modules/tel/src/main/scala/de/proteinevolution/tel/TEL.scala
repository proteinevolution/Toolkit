package de.proteinevolution.tel

import de.proteinevolution.tel.env.Env
import de.proteinevolution.tel.param.Params
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

/**
 * TEL is the access point to get ExecutionContexts in which runscripts can be executed
 *
 */
@Singleton
class TEL @Inject()(env: Env, params: Params, config: Configuration) extends TELRegex with TELConstants {

  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs: Seq[String] = Array("params", "results", "temp", "logs")

  val context: String = config.get[String]("submit_mode")

  /**
   * Returns the Array of all values and plain text names of the set params
   *
   * @param param
   */
  def generateValues(param: String): Map[String, String] = params.generateValues(param)
}

object TEL {

  var memFactor: Double = 1

  var threadsFactor: Double = 1

}
