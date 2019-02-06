package modules

import better.files._
import de.proteinevolution.tel.env.{ ExecFile, PropFile, TELEnv }
import javax.inject.{ Inject, Provider }
import play.api.{ Configuration, Logging }

class TELEnvProvider @Inject()(tv: TELEnv, configuration: Configuration) extends Provider[TELEnv] with Logging {

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
          case Some(".prop") => new PropFile(file.pathAsString, configuration).addObserver(tv)
          case Some(".sh")   => new ExecFile(file.pathAsString).addObserver(tv)
          case _             => ()
        }
      }
    tv
  }

}
