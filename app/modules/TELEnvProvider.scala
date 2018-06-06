package modules

import javax.inject.{ Inject, Provider }
import better.files._
import de.proteinevolution.tel.env.{ ExecFile, PropFile, TELEnv }
import play.api.{ Configuration, Logger }

class TELEnvProvider @Inject()(tv: TELEnv, configuration: Configuration) extends Provider[TELEnv] {

  override def get(): TELEnv = {
    configuration
      .get[Option[String]]("tel.env")
      .getOrElse {
        val fallBackFile = "tel/env"
        Logger.warn(s"Key 'tel.env' was not found in configuration. Fall back to '$fallBackFile'");
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
