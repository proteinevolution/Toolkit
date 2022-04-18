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

package de.proteinevolution.tel.param

import play.api.Logging

import javax.inject.Singleton
import scala.collection.immutable.ListMap

@Singleton
class ParamCollector extends Params with Logging {

  // Maps Parameter name to the underlying object
  private var generativeParams: ListMap[String, GenerativeParam] = ListMap.empty

  def reloadValues(): Unit = generativeParams.values.foreach { p =>
    p.load()
    logger.info(s"reloading generative param ${p.name}")
  }

  def generateValues(name: String): ListMap[String, String] =
    generativeParams(name).generate

  def addParam(name: String, param: GenerativeParam): Unit = {
    generativeParams = generativeParams + (name -> param)
  }
}
