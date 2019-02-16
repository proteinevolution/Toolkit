package de.proteinevolution.tools.forms

import de.proteinevolution.common.models.database.jobs.JobState
import io.circe.generic.JsonCodec

@JsonCodec case class JobForm(
    jobID: String,
    state: JobState,
    dateCreated: String,
    toolitem: ToolForm,
    views: Seq[String],
    paramValues: Map[String, String]
)
