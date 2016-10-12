package models.tel.param

/**
  * Created by lzimmermann on 10/11/16.
  */
abstract class Param(val name : String)

abstract class PredicativeParam(name : String) extends Param(name) {

  /**
    * Decides whether or not the value for this parameter is allowed
    * @param value The value that should be tested for its validity
    * @return Whether the provided value is valid for this parameter
    */
  def validate(value : String) : Boolean
}

abstract class GenerativeParam(name : String) extends PredicativeParam(name) with GenerateWithClearText  {

  // Sequence of allowed values with respective clear text name
  def validate(value : String) = this.generate.contains(value)
  def generate : Set[String]
}



trait GenerateWithClearText {

  def generateWithClearText : Map[String, String]
}






