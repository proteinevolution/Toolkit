package modules.tel.runscripts

/**
  * Created by lzimmermann on 07.12.16.
  */







class Parameter[A] {

    // A constraint decides for a value its validity
    type Constraint = A => Boolean

    // Map constraint name to the actual constraint
    val constraints: Map[String, Constraint] = Map.empty

  

}



/**
 * Encompasses the value representation of a runscript parameter.
 */
abstract class ValueRepresentation[A](value: A , executionContext: ExecutionContext) extends Cleanable{

  def represent: String
}




class LiteralRepresentation[A](value : A, executionContext : ExecutionContext)
  extends ValueRepresentation(value, executionContext) {

  private val internalValue = value

  def represent : String = value.toString

  override def clean(): Unit = {}
}


/**
  * Represents resources that can be cleaned, such that all occupied resources or created files in the filesystem
  * are cleared
  *
  */
trait Cleanable {

  def clean(): Unit
}


