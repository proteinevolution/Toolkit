/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.tel

import de.proteinevolution.tel.param.Params
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

/**
 * TEL is the access point to get ExecutionContexts in which runscripts can be executed
 *
 */
@Singleton
class TEL @Inject()(params: Params, config: Configuration) extends TELRegex with TELConstants {

  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Seq("jobID", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs: Seq[String] = Seq("params", "results", "temp", "logs")

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
