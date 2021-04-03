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

package de.proteinevolution.tools

import de.proteinevolution.tel.param.ParamCollector
import play.api.{Configuration, Logging}
import fs2.Stream
import cats.effect.{IO, Resource}
import fs2.io.file.Files
import better.files._
import fs2.io.Watcher

import java.nio.file.Path
import javax.inject.{Inject, Singleton}

@Singleton
class ConfigWatcher @Inject()(pc: ParamCollector,config: Configuration) extends Logging {

  private final val REFRESH_FILE = "tel.params_refresh"

  Stream.resource(configFile).flatMap{ f =>
    Files[IO].watch(f).map{
      case Watcher.Event.Modified(_, _) | Watcher.Event.Created(_, _) => pc.reloadValues()
      case Watcher.Event.Deleted(_, _) => logger.warn(s"file $REFRESH_FILE was deleted")
      case Watcher.Event.Overflow(_) => logger.warn(s"file $REFRESH_FILE overflow")
      case Watcher.Event.NonStandard(_, _) => logger.warn(s"file $REFRESH_FILE changed unexpectedly")
    }
  }.compile.drain

  private def configFile: Resource[IO, Path] =
    Resource.eval(IO.
      fromOption(config.get[Option[String]](REFRESH_FILE))(new IllegalArgumentException(s"file $REFRESH_FILE is missing")).map {
      _.toFile.path
    })

}
