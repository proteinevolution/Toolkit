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

import de.proteinevolution.tel.env.Env
import javax.inject.{ Inject, Provider }
import play.api.Configuration

import scala.collection.mutable

class EnvProvider @Inject()(configuration: Configuration) extends Provider[Env] {

  private val m = mutable.Map(configuration.get[Map[String, String]]("tel.env_vars").toSeq: _*)

  override def get(): Env = new Env {
    def get(key: String): String = m.getOrElse(key, "")

    // Adds a Key value pair to this environment
    def configure(key: String, value: String): Unit = m.update(key, value)
  }

}
