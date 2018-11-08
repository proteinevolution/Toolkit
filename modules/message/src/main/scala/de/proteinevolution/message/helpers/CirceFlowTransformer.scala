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
