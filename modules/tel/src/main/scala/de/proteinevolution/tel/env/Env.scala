package de.proteinevolution.tel.env
import scala.collection.mutable

/**
 * An Env is anything that provides key/value pairs
 */
trait Env {

  def get(key: String): String

  // Adds a Key value pair to this environment
  def configure(key: String, value: String): Unit

}
object Env {
  implicit def mapToEnv(m: mutable.Map[String, String]): Env = new Env {
    def get(key: String): String = m.getOrElse(key, "")

    // Adds a Key value pair to this environment
    def configure(key: String, value: String): Unit = m.update(key, value)
  }
}

/**
 * Something being EnvAware changes its behavior depending on the attached environment
 *
 */
trait EnvAware[A] {

  def withEnvironment(env: Env): A

}
