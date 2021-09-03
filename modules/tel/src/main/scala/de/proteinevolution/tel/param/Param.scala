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

package de.proteinevolution.tel.param

import de.proteinevolution.tel.TELRegex

import scala.collection.immutable.ListMap

abstract class Param(val name: String) extends TELRegex

abstract class PredicativeParam(name: String) extends Param(name) {

  /**
   * Decides whether or not the value for this parameter is allowed
   *
   * @param value
   *   The value that should be tested for its validity
   * @return
   *   Whether the provided value is valid for this parameter
   */
  def validate(value: String): Boolean
}

abstract class GenerativeParam(name: String) extends PredicativeParam(name) {

  // Sequence of allowed values with respective clear text name
  def validate(value: String): Boolean = this.generate.contains(value)

  def generate: ListMap[String, String]

  def load(): Unit
}
