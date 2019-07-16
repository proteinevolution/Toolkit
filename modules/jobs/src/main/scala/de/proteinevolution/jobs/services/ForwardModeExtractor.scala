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

package de.proteinevolution.jobs.services

import de.proteinevolution.jobs.models.ForwardMode
import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird.PathBindableExtractor

trait ForwardModeExtractor {

  implicit object forwardModeBindable
      extends Parsing[ForwardMode](
        _.trim match {
          case validString if ("alnEval" :: "evalFull" :: "aln" :: "full" :: Nil).contains(validString) =>
            ForwardMode(validString)
          case _ => throw new IllegalArgumentException
        },
        _.toString,
        (_: String, _: Exception) => "string not a valid forwarding mode"
      )

  val forwardModeExtractor: PathBindableExtractor[ForwardMode] =
    new PathBindableExtractor[ForwardMode]

}
