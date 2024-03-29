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

package controllers

import build.BuildInfo
import de.proteinevolution.base.controllers.ToolkitController
import javax.inject.{ Inject, Singleton }
import play.api.mvc._

@Singleton
class UptimeController @Inject() (cc: ControllerComponents) extends ToolkitController(cc) {

  private val startTime: Long = System.currentTimeMillis()

  def uptime: Action[AnyContent] =
    Action {
      val uptimeInMillis = System.currentTimeMillis() - startTime
      Ok(s"$uptimeInMillis ms")
    }

  def buildInfo: Action[AnyContent] =
    Action {
      Ok(s"${BuildInfo.toString}")
    }

}
