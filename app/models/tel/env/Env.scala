package models.tel.env

import com.google.inject.ImplementedBy


/**
  * An Env is anything that provides key/value pairs
  */
@ImplementedBy(classOf[TELEnv])
trait Env {

  def get(key : String) : String
}
