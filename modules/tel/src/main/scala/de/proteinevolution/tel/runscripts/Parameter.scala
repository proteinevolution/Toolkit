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

import better.files.File

class Parameter(val name: String) {

  // A constraint decides for a value its validity
  type Constraint = RType => Boolean

  private var constraints: Map[String, Constraint] = Map.empty[String, Constraint]

  def withConstraint(name: String, constraint: Constraint): Parameter = {
    constraints = constraints.updated(name, constraint)
    this
  }
  def withoutConstraint(name: String): Parameter = {
    constraints = constraints - name
    this
  }
}

/**
 * Encompasses the value representation of a runscript parameter.
 */
abstract class Representation {
  def represent: String
}

/**
 * Represents Parameter values which can be literally represented by the String value of an arbitrary type 'A'.
 *
 * @param value
 */
class LiteralRepresentation(value: RType) extends Representation {
  def represent: String = value.inner().toString
}

class FileRepresentation(file: File) extends Representation {
  def represent: String = file.pathAsString
}
