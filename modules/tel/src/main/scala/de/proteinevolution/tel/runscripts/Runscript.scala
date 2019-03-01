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

package de.proteinevolution.tel.runscripts

import better.files._
import de.proteinevolution.tel.TELRegex
import de.proteinevolution.tel.execution.ExecutionContext
import de.proteinevolution.tel.runscripts.Runscript.Evaluation
import play.api.Logging

import scala.collection.mutable
import scala.util.matching.Regex

/**
 * Represents one particular runscript, specified by the path of the corresponding file.
 * Instances should be created via the companion object.
 *
 */
class Runscript(files: Seq[File]) extends TELRegex with Logging {

  val parameters: Seq[(String, Evaluation)] = parameterString
    .findAllIn(files.map(_.contentAsString).mkString("\n"))
    .matchData
    .foldLeft(Seq.empty[(String, Evaluation)]) { (a, m) =>
      val paramName = m.group("paramName")
      a :+ paramName -> { (value: RType, executionContext: ExecutionContext) =>
        m.group("repr") match {
          // TODO Constraints are not yet supported, currently all arguments are valid
          case "path" =>
            ValidArgument(new FileRepresentation(executionContext.getFile(paramName, value.inner().toString)))
          case "content" => ValidArgument(new LiteralRepresentation(value))
        }
      }
    }

  // Implications with names for the parameters // TODO Currently not supported
  type Condition = (String, RType => Boolean, String, RType => Boolean)

  final val parameterNames
    : Seq[String] = parameters.map(_._1).distinct // Names of the parameters that need to be supplied

  // Special fields to put the runscript into a larger context

  private case class Replacer(arguments: Seq[(String, ValidArgument)]) {
    private var counter = -1
    def apply(m: Regex.Match): String = {
      m.groupNames.foreach(s => logger.debug(s)) // just use m because of https://stackoverflow.com/questions/43964571/scala-2-12-2-emits-a-ton-of-useless-warning-parameter-value-in-method
      counter += 1
      arguments(counter)._2.representation.represent
    }
  }

  private val translationSteps = mutable.Queue[String => String](_ => files.map(_.contentAsString).mkString("\n"))

  // Translates A sequence of Arguments with the parameter names into a runnable runscript instance
  def apply(arguments: Seq[(String, ValidArgument)]): String = {

    // Dequeue all transformers
    var init = translationSteps.dequeue()("")
    while (translationSteps.nonEmpty) {

      init = translationSteps.dequeue()(init)
    }

    val replacer = Replacer(arguments)
    parameterString.replaceAllIn(init, replacer.apply _)
  }

  def withEnvironment(env: Map[String, String]): Runscript = {

    translationSteps.enqueue { s =>
      envString.replaceAllIn(s, m => env.getOrElse(m.group("constant"), ""))
    }
    this
  }
}

object Runscript extends TELRegex {

  // An evaluation returns an argument given a value for a runscript type and an execution Context
  type Evaluation = (RType, ExecutionContext) => Argument

  /**
   * Reads the lines of a runscript file and returns a new runscript instance
   *
   */
  def apply(files: Seq[File]): Runscript = new Runscript(files)

}
