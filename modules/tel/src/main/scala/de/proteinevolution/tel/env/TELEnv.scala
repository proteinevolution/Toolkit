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

package de.proteinevolution.tel.env

import javax.inject.Singleton
import de.proteinevolution.tel.Observer

/**
 * Manages values of Keys from the TEL environment
 *
 */
@Singleton
class TELEnv extends Env with Observer[EnvFile] {

  @volatile private var env: Map[String, String] = Map.empty

  override def get(key: String): String = this.env(key)

  override def configure(key: String, value: String): Unit = {
    this.env = this.env + (key -> value)
  }

  override def remove(key: String): Unit = {
    this.env -= key
  }

  override def receiveInitial(subject: EnvFile): Unit = receiveUpdate(subject)

  override def receiveUpdate(subject: EnvFile): Unit = {

    // If the Environmental file triggers a change, reload it and add new variables to the
    // env
    subject.load.foreach { kv =>
      this.env = this.env + kv
    }
  }

}
