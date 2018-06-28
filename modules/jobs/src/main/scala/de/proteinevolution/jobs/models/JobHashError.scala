package de.proteinevolution.jobs.models

import play.api.libs.json._

trait JobHashError {
  def msg: JsValue
}

object JobHashError {

  private[this] def errors(names: String*): JsValue = {
    JsError.toJson(JsError(names.map { name =>
      (__ \ name, JsonValidationError("invalid") :: Nil)
    }))
  }

  case object JobNotFound extends JobHashError {
    val msg: JsValue = errors("job not found in database")
  }

  case object JobIsPrivate extends JobHashError {
    val msg: JsValue = errors("job not found")
  }

}
