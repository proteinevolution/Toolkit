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
import cats.effect.{IO, Resource, Ref}
import fs2.io.file.Files
import better.files._
import fs2.io.Watcher

import java.nio.file.Path
import javax.inject.{Inject, Singleton}
import cats.effect.unsafe.implicits.global

@Singleton
final private[tools] class ConfigWatcher @Inject()(
    pc: ParamCollector,
    config: Configuration,
    toolConfig: ToolConfig
) extends Logging {

  private[this] final val REFRESH_FILE = "tel.params_refresh"

  // start the file watcher
  toolConfig.ref
    .flatMap(watch)
    .unsafeRunSync()

  // fs2 file watcher
  private[this] def watch(r: Ref[IO, Map[String, Tool]]): IO[Unit] =
    Stream
      .resource(configFile)
      .flatMap { f =>
        Files[IO].watch(f).map {
          case Watcher.Event.Modified(_, _) | Watcher.Event.Created(_, _) =>
            logger.info(s"file $REFRESH_FILE changed, reloading parameters...")
            r.update(_ => toolConfig.readFromFile()) // update since the config has to be re-parsed
            pc.reloadValues() // reload params
          case Watcher.Event.Deleted(_, _) =>
            logger.warn(s"file $REFRESH_FILE was deleted")
          case Watcher.Event.Overflow(_) =>
            logger.warn(s"file $REFRESH_FILE overflow")
          case Watcher.Event.NonStandard(_, _) =>
            logger.warn(s"file $REFRESH_FILE changed unexpectedly")
        }
      }
      .compile
      .drain

  // wrap the file path in a `Resource[IO, Path]` so that it can be consumed by a fs2 Stream
  private[this] def configFile: Resource[IO, Path] =
    Resource.eval(
      IO.fromOption(config.get[Option[String]](REFRESH_FILE))(
          new IllegalArgumentException(s"file $REFRESH_FILE is missing"))
        .map {
          _.toFile.path
        })

}
