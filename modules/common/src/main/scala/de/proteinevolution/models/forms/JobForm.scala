package de.proteinevolution.models.forms

import de.proteinevolution.models.database.jobs.JobState
import io.circe.generic.JsonCodec

@JsonCodec case class JobForm(
    jobID: String,
    state: JobState,
    dateCreated: String,
    toolitem: ToolForm,
    views: Seq[String],
    paramValues: Map[String, String]
)
