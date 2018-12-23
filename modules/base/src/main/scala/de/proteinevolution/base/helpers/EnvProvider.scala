package de.proteinevolution.base.helpers

import play.Environment
import play.api.Configuration

object EnvProvider {

  def get(config: Configuration, environment: Environment): String = {
    val envPath = config.get[String]("environment")
    if (environment.isDev) {
      environment.rootPath().getAbsolutePath + "/" + envPath
    } else {
      envPath
    }
  }

}
