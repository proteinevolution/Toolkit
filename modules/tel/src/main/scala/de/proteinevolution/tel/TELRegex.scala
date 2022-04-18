/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import scala.util.matching.Regex

trait TELRegex {

  // For translating the runscript template into an executable instance
  val replaceString: Regex = """%([A-Za-z_\.]+)""".r("expression") // TODO Not needed anymore, remove

  // A constant string starts with a percent sign
  final val envString: Regex = """%([A-Z]+)""".r("constant")
  // A parameter String in an runscript starts with a percent sign, a parameter name and a representation
  final val parameterString: Regex = """%([a-z_]+)\.([a-z_]+)""".r("paramName", "repr")

  val runscriptString: Regex = """%r""".r
  val regexJobID: Regex      = """%JOBID""".r
  val regexPort: Regex       = """%PORT""".r
}
