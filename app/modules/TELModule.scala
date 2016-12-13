package modules

import javax.inject.{Inject, Provider}

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import modules.tel.env.{Env, ExecFile, PropFile, TELEnv}
import modules.tel.param.{GenerativeParamFileParser, ParamCollector, Params}
import play.api.{Configuration, Logger}
import com.google.inject.name.Names
import modules.tel.execution.EngineExecution

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
          .annotatedWith(Names.named("enginePath"))
          .toProvider(classOf[EnginePathProvider])
          .asEagerSingleton()

        // Install the Factory for EngineExecution
        install(new FactoryModuleBuilder().build(classOf[EngineExecution.Factory]))
  }
}



/*
install(new FactoryModuleBuilder()
     .implement(Payment.class, RealPayment.class)
     .build(PaymentFactory.class));

 */

import better.files._


class EnginePathProvider @Inject() (configuration: Configuration) extends Provider[String] {

  override def get(): String = {

    configuration.getString("tel.engine").getOrElse {
      Logger.warn("Key 'tel.engine' was not found in configuration. Fall back to 'tel/engine'")
      "tel/engine"
    }
  }
}


class RunscriptPathProvider @Inject() (configuration: Configuration) extends Provider[String] {


  override def get(): String = {

    configuration.getString("tel.runscripts").getOrElse{
      Logger.warn("Key 'tel.runscripts' was not found in configuration. Fall back to 'tel/runscripts'")
      "tel/runscripts"
    }
  }
}


class ParamCollectorProvider @Inject() (pc : ParamCollector, configuration: Configuration) extends Provider[ParamCollector] {

  override def get(): ParamCollector = {

      val paramFilePath = configuration.getString("tel.params").getOrElse{
        Logger.warn("Key 'tel.params' was not found in configuration. Fall back to 'tel/paramspec/PARAMS'")
        "tel/paramspec/PARAMS"
      }
      GenerativeParamFileParser.read(paramFilePath).foreach { param =>

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

      Logger.warn("Key 'tel.env' was not found in configuration. Fall back to 'tel/env'") ;
      "tel/env"
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
