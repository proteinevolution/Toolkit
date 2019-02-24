/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
