package de.proteinevolution.tel

import javax.inject.{ Inject, Provider }

import de.proteinevolution.tel.param.{ GenerativeParamFileParser, ParamCollector }
import play.api.{ Configuration, Logger }

sealed trait TELProvider      extends Provider[String]
sealed trait TELParamProvider extends Provider[ParamCollector]

class WrapperPathProvider @Inject()(configuration: Configuration) extends TELProvider {

  private val logger = Logger(this.getClass)

  override def get(): String = {

    configuration.get[Option[String]]("tel.wrapper").getOrElse {
      val fallBackFile = "tel/wrapper.sh"
      logger.warn(s"Key 'tel.wrapper' was not found in configuration. Fall back to '$fallBackFile'")
      fallBackFile
    }
  }
}

class RunscriptPathProvider @Inject()(configuration: Configuration) extends TELProvider {

  private val logger = Logger(this.getClass)

  override def get(): String = {

    configuration.get[Option[String]]("tel.runscripts").getOrElse {
      val fallBackFile = "tel/runscripts"

      logger.warn(s"Key 'tel.runscripts' was not found in configuration. Fall back to '$fallBackFile'")
      fallBackFile
    }
  }
}

class ParamCollectorProvider @Inject()(
    pc: ParamCollector,
    configuration: Configuration,
    generativeParamFileParser: GenerativeParamFileParser
) extends TELParamProvider {

  private val logger = Logger(this.getClass)

  override def get(): ParamCollector = {

    lazy val paramFilePath = configuration.get[Option[String]]("tel.params").getOrElse {

      val fallBackFile = "tel/paramspec/PARAMS"
      logger.warn(s"Key 'tel.params' was not found in configuration. Fall back to '$fallBackFile'")
      fallBackFile
    }

    generativeParamFileParser.read(paramFilePath).foreach { param =>
      pc.addParam(param.name, param)

    }
    pc
  }
}
