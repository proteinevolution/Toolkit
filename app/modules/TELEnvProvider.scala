package modules

import javax.inject.{ Inject, Provider, Singleton }
import better.files._
import de.proteinevolution.base.helpers.EnvProvider
import de.proteinevolution.tel.env.{ ExecFile, PropFile, TELEnv }
import play.api.{ Configuration, Logger }

@Singleton
class TELEnvProvider @Inject()(tv: TELEnv, configuration: Configuration, environment: play.Environment)
    extends Provider[TELEnv] {

  private val logger = Logger(this.getClass)

  override def get(): TELEnv = {
    configuration
      .get[Option[String]]("tel.env")
      .getOrElse {
        val fallBackFile = "tel/env"
        logger.warn(s"Key 'tel.env' was not found in configuration. Fall back to '$fallBackFile'")
        fallBackFile
      }
      .toFile
      .list
      .foreach { file =>
        file.extension match {
          case Some(".prop") =>
            new PropFile(file.pathAsString, configuration, EnvProvider.get(configuration, environment)).addObserver(tv)
          case Some(".sh") => new ExecFile(file.pathAsString).addObserver(tv)
          case _           => ()
        }
      }
    tv
  }

}
