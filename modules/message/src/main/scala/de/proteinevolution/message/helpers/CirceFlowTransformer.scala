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

package de.proteinevolution.message.helpers

import akka.stream.scaladsl.Flow
import io.circe.Json
import io.circe.parser._
import play.api.http.websocket._
import play.api.libs.streams.AkkaStreams
import play.api.mvc.WebSocket.MessageFlowTransformer

import scala.util.control.NonFatal

object CirceFlowTransformer {

  /**
   * Converts messages to/from Json
   */
  implicit val jsonMessageFlowTransformer: MessageFlowTransformer[Json, Json] = {
    def closeOnException[T](block: => T): Either[T, CloseMessage] =
      try {
        Left(block)
      } catch {
        case NonFatal(_) => Right(CloseMessage(Some(CloseCodes.Unacceptable), "Unable to parse json message"))
      }

    flow: Flow[Json, Json, _] =>
      {
        AkkaStreams.bypassWith[Message, Json, Message](Flow[Message].collect {
          case BinaryMessage(data) =>
            Right(CloseMessage(Some(CloseCodes.Unacceptable), "does not deal with binary messages"))
          case TextMessage(text) => closeOnException(parse(text).toOption.getOrElse(Json.Null))
        })(flow.map { json =>
          TextMessage(json.noSpaces)
        })
      }
  }

}
