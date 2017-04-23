package modules

import java.nio.file.attribute.PosixFilePermission
import javax.inject.{Inject, Provider}

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import modules.tel.env.{Env, ExecFile, PropFile, TELEnv}
import modules.tel.param.{GenerativeParamFileParser, ParamCollector, Params}
import play.api.{Configuration, Logger}
import com.google.inject.name.Names
import grizzled.sys.OperatingSystem.Posix
import modules.tel.execution.WrapperExecutionFactory

/**
  * Created by lukas on 8/28/16.
  */
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



/*
install(new FactoryModuleBuilder()
     .implement(Payment.class, RealPayment.class)
     .build(PaymentFactory.class));

 */

import better.files._


class WrapperPathProvider @Inject() (configuration: Configuration) extends Provider[String] {

  override def get(): String = {

    configuration.getString("tel.wrapper").getOrElse {
      val fallBackFile = "tel/wrapper.sh"
      Logger.warn(s"Key 'tel.wrapper' was not found in configuration. Fall back to '$fallBackFile'")
      fallBackFile
    }
  }
}


class RunscriptPathProvider @Inject() (configuration: Configuration) extends Provider[String] {


  override def get(): String = {

    configuration.getString("tel.runscripts").getOrElse{
      val fallBackFile = "tel/runscripts"

      Logger.warn(s"Key 'tel.runscripts' was not found in configuration. Fall back to '$fallBackFile'")
     fallBackFile
    }
  }
}


class ParamCollectorProvider @Inject()
      (pc : ParamCollector,
       configuration: Configuration,
       generativeParamFileParser: GenerativeParamFileParser) extends Provider[ParamCollector] {

  override def get(): ParamCollector = {


     lazy val paramFilePath = configuration.getString("tel.params").getOrElse{

        val fallBackFile = "tel/paramspec/PARAMS"
        Logger.warn(s"Key 'tel.params' was not found in configuration. Fall back to '$fallBackFile'")
        fallBackFile
      }

      generativeParamFileParser.read(paramFilePath).foreach { param =>

        pc.addParam(param.name, param)

      }

    pc
  }
}


/**
  *  Uses the tel configuration to wire the TELEnv environment to the env module
  */
class TELEnvProvider @Inject()(tv : TELEnv, configuration: Configuration) extends  Provider[TELEnv] {

  override def get(): TELEnv = {

    // Try loading the environment files from the configured directory
    configuration.getString("tel.env").getOrElse {

      val fallBackFile = "tel/env"

      Logger.warn(s"Key 'tel.env' was not found in configuration. Fall back to '$fallBackFile'") ;
      fallBackFile
    }.toFile.list.foreach { file =>

      file.setPermissions(Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))

      file.extension match {

        case Some(".prop") => new PropFile(file.pathAsString).addObserver(tv)
        case Some(".sh") => new ExecFile(file.pathAsString).addObserver(tv)
        case _ => //
      }
    }
    tv
  }
}
