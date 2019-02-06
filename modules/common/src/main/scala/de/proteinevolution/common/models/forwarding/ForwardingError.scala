package de.proteinevolution.models.forwarding

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
