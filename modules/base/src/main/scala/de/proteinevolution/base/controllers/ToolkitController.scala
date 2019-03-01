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

package de.proteinevolution.base.controllers

import de.proteinevolution.base.helpers.ToolkitTypes
import io.circe.{ Json, JsonObject }
import io.circe.syntax._
import play.api.i18n.I18nSupport
import play.api.libs.circe.Circe
import play.api.mvc.{ AbstractController, ControllerComponents, Result }

abstract class ToolkitController(cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport
    with Circe
    with ToolkitTypes {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

  protected def errors(names: String*): Json = {
    JsonObject.fromMap(names.map(_ -> Json.fromString("invalid")).toMap).asJson
  }

}
