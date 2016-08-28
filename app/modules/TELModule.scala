package modules

import javax.inject.{Inject, Provider}

import com.google.inject.AbstractModule
import models.tel.env.{Env, ExecFile, PropFile, TELEnv}
import play.api.{Configuration, Logger}


/**
  * Created by lukas on 8/28/16.
  */
class TELModule  extends AbstractModule {


  override def configure() = {

        bind(classOf[Env]).toProvider(classOf[TELModuleProvider])
  }
}

class TELModuleProvider @Inject() (tv : TELEnv, configuration: Configuration) extends  Provider[TELEnv] {

  override def get() = {


    import better.files._
    // Try loading the environment files from the configured directory
    configuration.getString("tel.env").getOrElse {

      Logger.warn("Key 'tel.env' was not found in configuration. Fall back to 'tel/env'") ; "tel/env"
    }.toFile.list.foreach { file =>

      file.extension match {

        case Some(".prop") => new PropFile(file.pathAsString).addObserver(tv)
        case Some(".sh") => new ExecFile(file.pathAsString).addObserver(tv)
        case _ => //
      }
    }
    tv
  }
}
