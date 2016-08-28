package models.tel.env



/**
  * An Env is anything that provides key/value pairs
  */
trait Env {

  def get(key : String) : String
}
