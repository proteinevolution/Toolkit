package modules.tel.runscripts

import better.files.File


/**
  * Created by lzimmermann on 07.12.16.
  */



class Parameter(val name : String) {

  // A constraint decides for a value its validity
  type Constraint = RType => Boolean

    var constraints : Map[String, Constraint] = Map.empty[String, Constraint]

    def withConstraint(name : String, constraint: Constraint) : Parameter = {

      this.constraints = this.constraints.updated(name, constraint)
      this
    }
    def withoutConstraint(name : String) : Parameter = {

      this.constraints = this.constraints - name
      this
    }
}



/**
 * Encompasses the value representation of a runscript parameter.
  *
 */
abstract class Representation{

  def represent: String
}


/**
  * Represents Parameter values which can be literally represented by the String value of an arbitrary
  * type 'A'.
  *
  * @param value
  */
class LiteralRepresentation(value : RType) extends Representation {

  def represent : String = value.inner().toString
}

class FileRepresentation(file: File) extends Representation {

  def represent: String = file.pathAsString
}
