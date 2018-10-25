package de.proteinevolution.base.helpers

import akka.stream.scaladsl.Flow
import play.api.http.websocket._
import play.api.libs.streams.AkkaStreams
import play.api.mvc.WebSocket.MessageFlowTransformer
import scala.util.control.NonFatal
import io.circe.Json

/*
  Websocket handler for circe
 */

object CirceFlowTransformer {

  /**
   * Converts messages to/from Json
   */
  def jsonMessageFlowTransformer: MessageFlowTransformer[Json, Json] = {
    def closeOnException[T](block: => T): Either[T, CloseMessage] =
      try {
        Left(block)
      } catch {
        case NonFatal(_) => Right(CloseMessage(Some(CloseCodes.Unacceptable), "Unable to parse json message"))
      }

    flow: Flow[Json, Json, _] =>
      {
        AkkaStreams.bypassWith[Message, Json, Message](Flow[Message].collect {
          // case BinaryMessage(data) => closeOnException(Json.(data.iterator.asInputStream))
          case TextMessage(text) => closeOnException(Json.fromString(text))
        })(flow.map { json =>
          TextMessage(json.noSpaces)
        })
      }
  }

}
