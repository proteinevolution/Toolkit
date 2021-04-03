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
import play.api.Configuration
import fs2.Stream
import cats.effect.{IO, Resource}
import fs2.io.file.Files
import better.files._

import javax.inject.{Inject, Singleton}

@Singleton
class ConfigWatcher @Inject()(pc: ParamCollector,config: Configuration) {

  Stream.resource(for {
    f <- Resource.eval(IO.fromOption(config.get[Option[String]]("tel.params_refresh"))(new IllegalArgumentException(s"file tel.params_refresh is missing")))
  } yield {
    f.toFile.path
  }).flatMap{ f =>
    Files[IO].watch(f, Nil).map(_ => pc.reloadValues())
  }.compile.drain

}
