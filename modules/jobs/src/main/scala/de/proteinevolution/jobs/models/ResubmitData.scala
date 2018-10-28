package de.proteinevolution.jobs.models

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class ResubmitData(exists: Boolean, version: Option[Int], suggested: Option[String])

object ResubmitData {

  implicit val resubmitDataEncoder: Encoder[ResubmitData] = deriveEncoder

}
