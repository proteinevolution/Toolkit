package modules

import javax.inject.{ Inject, Provider }

import com.google.inject.AbstractModule
import de.proteinevolution.tel.env.{ Env, ExecFile, PropFile, TELEnv }
import de.proteinevolution.tel.param.Params
import play.api.{ Configuration, Logger }
import com.google.inject.name.Names
import de.proteinevolution.tel.{ ParamCollectorProvider, RunscriptPathProvider, WrapperPathProvider }
import better.files._

class TELModule extends AbstractModule {

  override def configure(): Unit = {

    // Bind TEL Env
    bind(classOf[Env])
      .toProvider(classOf[TELEnvProvider])
      .asEagerSingleton()

    // Bind TEL Parameter Collector
    bind(classOf[Params])
      .toProvider(classOf[ParamCollectorProvider])
      .asEagerSingleton()

    bind(classOf[String])
      .annotatedWith(Names.named("runscriptPath"))
      .toProvider(classOf[RunscriptPathProvider])
      .asEagerSingleton()

    bind(classOf[String])
      .annotatedWith(Names.named("wrapperPath"))
      .toProvider(classOf[WrapperPathProvider])
      .asEagerSingleton()
  }
}

/**
 *  Uses the tel configuration to wire the TELEnv environment to the env module
 */
class TELEnvProvider @Inject()(tv: TELEnv, configuration: Configuration) extends Provider[TELEnv] {

  override def get(): TELEnv = {

    // Try loading the environment files from the configured directory
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

          case Some(".prop") => new PropFile(file.pathAsString).addObserver(tv)
          case Some(".sh")   => new ExecFile(file.pathAsString).addObserver(tv)
          case _             => //
        }
      }
    tv
  }
}
