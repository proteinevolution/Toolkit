package de.proteinevolution.models.forms

import de.proteinevolution.models.database.jobs.JobState
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{ JsPath, Writes }
import play.api.libs.functional.syntax._

// Server returns such an object when asked for a job
case class JobForm(jobID: String,
                   state: JobState,
                   dateCreated: String,
                   toolitem: ToolForm,
                   views: Seq[String],
                   paramValues: Map[String, String])

object JobForm {

  implicit val jobFormWrites: Writes[JobForm] = (
    (JsPath \ "jobID").write[String] and
    (JsPath \ "state").write[JobState] and
    (JsPath \ "dateCreated").write[String] and
    (JsPath \ "toolitem").write[ToolForm] and
    (JsPath \ "views").write[Seq[String]] and
    (JsPath \ "paramValues").write[Map[String, String]](play.api.libs.json.Writes.mapWrites[String])
  )(unlift(JobForm.unapply))

}
