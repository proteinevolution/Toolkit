package de.proteinevolution.message.helpers

import akka.stream.scaladsl.Flow
import io.circe.Json
import play.api.http.websocket._
import play.api.libs.streams.AkkaStreams
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.util.ByteString
import scala.util.control.NonFatal

object CirceFlowTransformer {

  implicit val identityMessageFlowTransformer: MessageFlowTransformer[Message, Message] = {
    new MessageFlowTransformer[Message, Message] {
      def transform(flow: Flow[Message, Message, _]) = flow
    }
  }

  /**
   * Converts text messages to/from Strings.
   */
  implicit val stringMessageFlowTransformer: MessageFlowTransformer[String, String] = {
    new MessageFlowTransformer[String, String] {
      def transform(flow: Flow[String, String, _]) = {
        AkkaStreams.bypassWith[Message, String, Message](Flow[Message].collect {
          case TextMessage(text) => Left(text)
          case BinaryMessage(_) =>
            Right(CloseMessage(Some(CloseCodes.Unacceptable), "This WebSocket only supports text frames"))
        })(flow.map(TextMessage.apply))
      }
    }
  }

  /**
   * Converts binary messages to/from ByteStrings.
   */
  implicit val byteStringMessageFlowTransformer: MessageFlowTransformer[ByteString, ByteString] = {
    new MessageFlowTransformer[ByteString, ByteString] {
      def transform(flow: Flow[ByteString, ByteString, _]) = {
        import akka.util.ByteString
        AkkaStreams.bypassWith[Message, ByteString, Message](Flow[Message].collect {
          case BinaryMessage(data) => Left(data)
          case TextMessage(_) =>
            Right(CloseMessage(Some(CloseCodes.Unacceptable), "This WebSocket only supports binary frames"))
        })(flow.map(BinaryMessage.apply))
      }
    }
  }

  /**
   * Converts binary messages to/from byte arrays.
   */
  implicit val byteArrayMessageFlowTransformer: MessageFlowTransformer[Array[Byte], Array[Byte]] = {
    byteStringMessageFlowTransformer.map(_.toArray, ByteString.apply)
  }

  import io.circe.parser._

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
