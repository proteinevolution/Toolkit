package de.proteinevolution.tel.runscripts

import better.files.File

class Parameter(val name: String) {

  // A constraint decides for a value its validity
  type Constraint = RType => Boolean

  private var constraints: Map[String, Constraint] =
    Map.empty[String, Constraint]

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
 *
 */
abstract class Representation {
  def represent: String
}

/**
 * Represents Parameter values which can be literally represented by the String value of an arbitrary
 * type 'A'.
 *
 * @param value
 */
class LiteralRepresentation(value: RType) extends Representation {
  def represent: String = value.inner().toString
}

class FileRepresentation(file: File) extends Representation {
  def represent: String = file.pathAsString
}
