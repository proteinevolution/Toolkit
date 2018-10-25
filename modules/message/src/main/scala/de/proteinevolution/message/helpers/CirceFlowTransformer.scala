package de.proteinevolution.message.helpers

import akka.stream.scaladsl.Flow
import io.circe.Json
import io.circe.parser.parse
import play.api.http.websocket._
import play.api.libs.streams.AkkaStreams
import play.api.mvc.WebSocket.MessageFlowTransformer

object CirceFlowTransformer {

  implicit val jsonMessageFlowTransformer: MessageFlowTransformer[Json, Json] =
    (flow: Flow[Json, Json, _]) =>
      AkkaStreams.bypassWith[Message, Json, Message](Flow[Message].collect {
        case BinaryMessage(_) =>
          Right(CloseMessage(Some(CloseCodes.Unacceptable), "does not deal with binary messages"))
        case TextMessage(txtMsg) =>
          parse(txtMsg) match {
            case Left(err) =>
              Right(CloseMessage(Some(CloseCodes.Unacceptable), err.getMessage()))
            case Right(json) => Left(json)
          }
      })(flow.map(json => TextMessage(json.noSpaces): Message))

}
