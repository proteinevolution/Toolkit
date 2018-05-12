package de.proteinevolution.models.forwarding

import play.api.libs.json._

trait ForwardingError {

  def message: JsValue

}

object ForwardingError {

  private[this] def errors(names: String*): JsValue = {
    JsError.toJson(JsError(names.map { name =>
      (__ \ name, JsonValidationError("invalid") :: Nil)
    }))
  }

  case object InvalidModal extends ForwardingError {
    val message: JsValue = errors("Invalid Forwarding Options")
  }

}
