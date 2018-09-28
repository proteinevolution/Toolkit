package de.proteinevolution.models.param

import play.api.libs.json._

sealed trait ParamType {

  /**
   * Parses the value and return the same value as Option if valid, otherwise None
   * @param value String value to be validated
   * @return Some(value) if value is valid, else None
   */
  def validate(value: String): Option[String]
}

object ParamType {

  case class Sequence(formats: Seq[(String, String)], placeholder: String, allowTwoTextAreas: Boolean)
      extends ParamType {
    // Sequence currently always valid
    def validate(value: String): Option[String] = Some(value)
  }

  case class Number(min: Option[Int], max: Option[Int]) extends ParamType {
    def validate(value: String): Option[String] = {
      for {
        ⌊ ← min
        ⌉ ← max
        if ⌊ <= value.toDouble && value.toDouble <= ⌉
      } yield {
        val _ = (⌊, ⌉)
        value
      }
    }
  }
  case class Select(options: Seq[(String, String)]) extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value).filter(options.map {
        case (key, _) => key
      }.contains)
    }
  }

  case object Bool extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value)
    }
  }

  case object Radio extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value)
    }
  }
  case class Decimal(step: String, min: Option[Double], max: Option[Double]) extends ParamType {
    def validate(value: String): Option[String] = {
      for {
        ⌊ ← min
        ⌉ ← max
        if ⌉ - value.toDouble > ⌊
      } yield {
        val _ = (⌊, ⌉)
        value
      }
    }
  }

  case class Text(placeholder: String = "") extends ParamType {
    def validate(value: String): Option[String] = Some(value)
  }

  case object ModellerKey extends ParamType {
    def validate(value: String): Option[String] = Some(value)
  }

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A, B)] = new Writes[(A, B)] {
    def writes(tuple: (A, B)) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }

  final val UnconstrainedNumber = Number(None, None)
  final val Percentage          = Number(Some(0), Some(100))
  final val ConstrainedNumber   = Number(Some(1), Some(10000))
  final val FIELD_TYPE          = "type"

  implicit object ParamTypeWrites extends Writes[ParamType] {
    def writes(paramType: ParamType): JsObject = paramType match {
      case Sequence(formats: Seq[(String, String)], placeholder: String, allowsTwoTextAreas: Boolean) =>
        Json.obj(FIELD_TYPE           -> 1,
                 "modes"              -> formats,
                 "allowsTwoTextAreas" -> allowsTwoTextAreas,
                 "placeholder"        -> placeholder)
      case Number(minOpt, maxOpt)        => Json.obj(FIELD_TYPE -> 2, "min" -> minOpt, "max" -> maxOpt)
      case Select(options)               => Json.obj(FIELD_TYPE -> 3, "options" -> options)
      case Bool                          => Json.obj(FIELD_TYPE -> 4)
      case Radio                         => Json.obj(FIELD_TYPE -> 5)
      case Decimal(step, minVal, maxVal) => Json.obj(FIELD_TYPE -> 2, "step" -> step, "min" -> minVal, "max" -> maxVal)
      case Text(placeholder)             => Json.obj(FIELD_TYPE -> 7, "placeholder" -> placeholder)
      case ModellerKey                   => Json.obj(FIELD_TYPE -> 8)
    }
  }
}
