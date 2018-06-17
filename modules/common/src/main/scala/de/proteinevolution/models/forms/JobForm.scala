package de.proteinevolution.models.forms

import de.proteinevolution.models.database.jobs.JobState._
import play.api.libs.json._

// Server returns such an object when asked for a job
case class JobForm(
    jobID: String,
    state: JobState,
    dateCreated: String,
    toolitem: ToolForm,
    views: Seq[String],
    paramValues: Map[String, String]
)

object JobForm {

  implicit val jobFormWrites: OWrites[JobForm] = Json.writes[JobForm]

}
