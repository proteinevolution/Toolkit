package de.proteinevolution.jobs.models

import play.api.libs.json._

final private[jobs] case class ResubmitData(
    exists: Boolean,
    version: Option[Int],
    suggested: Option[String]
)

object ResubmitData {

  implicit val resubmitDataWrites: OWrites[ResubmitData] =
    Json.writes[ResubmitData]

}
