/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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
import play.api.{ Configuration, Logging }
import better.files._
import cats.effect.{ IO, Resource }
import fs2.Stream
import fs2.io.file.Files
import fs2.io.file.{ Path => FS2Path }
import fs2.io.file.Watcher
import cats.effect.unsafe.implicits.global

import java.util.concurrent.atomic.AtomicReference
import javax.inject.{ Inject, Singleton }

@Singleton
final private[tools] class ConfigWatcher @Inject() (
    pc: ParamCollector,
    config: Configuration,
    toolConfig: ToolConfig
) extends Logging {

  private[this] final val REFRESH_FILE = "tel.params_refresh"

  // start the file watcher
  (for {
    refreshFile <- IO.fromOption(config.get[Option[String]](REFRESH_FILE))(
      new IllegalArgumentException(s"configured file $REFRESH_FILE is missing")
    )
    path = refreshFile.toFile.path
    _ <- IO(logger.info(s"using $path as trigger for param reload"))
  } yield FS2Path.fromNioPath(path))
    .flatMap { p =>
      watch(p, toolConfig.ref).repeat.compile.drain
    }
    .unsafeRunAsync(_ => ())

  // fs2 file watcher
  private[this] def watch(
      path: FS2Path,
      ref: AtomicReference[Map[String, Tool]]
  ): Stream[IO, Unit] =
    Stream.resource(Resource.eval(IO(path))).flatMap { f =>
      Files[IO].watch(f).evalMap {
        case Watcher.Event.Modified(_, _) | Watcher.Event.Created(_, _) =>
          for {
            _ <- IO(logger.info(s"file $path changed, reloading parameters ..."))
            _ <- IO(ref.set(toolConfig.readFromFile()))
            _ <- IO(logger.info(s"updated tool config"))
            _ <- IO(pc.reloadValues())
          } yield ()
        case Watcher.Event.Deleted(_, _) =>
          IO(logger.warn(s"file $path was deleted"))
        case Watcher.Event.Overflow(_) =>
          IO(logger.warn(s"file $path overflow"))
        case Watcher.Event.NonStandard(_, _) =>
          IO(logger.warn(s"file $path changed unexpectedly"))
      }
    }

}
