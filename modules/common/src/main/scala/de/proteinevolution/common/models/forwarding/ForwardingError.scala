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

package de.proteinevolution.common.models.forwarding

import io.circe.{ Json, JsonObject }
import io.circe.syntax._

sealed trait ForwardingError {
  def message: Json
}

object ForwardingError {

  protected def errors(names: String*): Json = {
    JsonObject.fromMap(names.map(_ -> Json.fromString("invalid")).toMap).asJson
  }

  case object InvalidModal extends ForwardingError {
    val message: Json = errors("Invalid Forwarding Options")
  }

}
