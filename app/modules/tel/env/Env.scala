package modules.tel.env



/**
  * An Env is anything that provides key/value pairs
  */
trait Env {

  def get(key : String) : String

  // Adds a Key value pair to this environment
  def configure(key: String, value: String): Unit
}
