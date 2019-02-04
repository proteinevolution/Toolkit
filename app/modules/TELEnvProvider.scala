package modules

import de.proteinevolution.tel.env.Env
import javax.inject.{Inject, Provider}
import play.api.Configuration

import scala.collection.mutable

class TELEnvProvider @Inject()(configuration: Configuration) extends Provider[Env] {

  override def get(): Env = {
    mutable.Map(configuration.get[Map[String, String]]("tel.env_vars").toSeq: _*)
  }

}
