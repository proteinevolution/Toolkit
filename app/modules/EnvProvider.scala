package modules

import de.proteinevolution.tel.env.Env
import javax.inject.{ Inject, Provider }
import play.api.Configuration

import scala.collection.mutable

class EnvProvider @Inject()(configuration: Configuration) extends Provider[Env] {

  private val m = mutable.Map(configuration.get[Map[String, String]]("tel.env_vars").toSeq: _*)

  override def get(): Env = new Env {
    def get(key: String): String = m.getOrElse(key, "")

    // Adds a Key value pair to this environment
    def configure(key: String, value: String): Unit = m.update(key, value)
  }

}
