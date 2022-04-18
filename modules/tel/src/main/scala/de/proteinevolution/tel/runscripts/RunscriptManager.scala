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

package de.proteinevolution.tel.runscripts

import better.files._
import javax.inject.{ Inject, Named, Singleton }
import play.api.Logging

//TODO
// * Support Constraints and Conditions
// * Runscript Manager should watch the runscript Path for changes

/**
 * Class watches the directory of runscripts and monitors changes. Reloads information about runscripts once the file
 * changes and keeps a map of all Runscripts for quick access. Allows to provide a runscript to an interested instance,
 * like a JobActor
 */
@Singleton
class RunscriptManager @Inject() (@Named("runscriptPath") runscriptPath: String) extends Logging {

  // Constants for the RunscriptManager
  final val SUFFIX = "SUFFIX"
  final val PREFIX = "PREFIX"

  logger.info(s"RunscriptManager started, surveilling: $runscriptPath")

  // Maps each runscript name to the corresponding file
  private final val runscripts: Map[String, File] = runscriptPath.toFile.list
    .withFilter(_.extension.getOrElse("") == ".sh")
    .map { file =>
      file.nameWithoutExtension -> file
    }
    .toMap
  // All files that will be prepended to a requested runscript
  private final val prefix: Seq[File] = {
    val x = runscriptPath.toFile / PREFIX
    if (x.exists) {
      logger.info("PREFIX will be automatically prepended to each runscript")
      Seq(x)
    } else {
      Seq.empty
    }
  }
  // All files that will be appended to a requested runscript
  private final val suffix: Seq[File] = {
    val x = runscriptPath.toFile / SUFFIX
    if (x.exists) {
      logger.info("SUFFIX will be automatically prepended to each runscript")
      Seq(x)
    } else {
      Seq.empty
    }
  }
  def apply(runscriptName: String): Runscript = Runscript(prefix ++ Seq(runscripts(runscriptName)) ++ suffix)
}

// An argument is either valid or invalid. If it is invalid, it can be represented, otherwise it has
// a collection of violated Constraints
sealed trait Argument
case class InvalidArgument(violatedConstraints: Seq[RType => Boolean]) extends Argument
case class ValidArgument(representation: Representation)               extends Argument

sealed trait RType {
  def inner(): String
}
case class RString(x: String) extends RType {
  def inner(): String = x
}
