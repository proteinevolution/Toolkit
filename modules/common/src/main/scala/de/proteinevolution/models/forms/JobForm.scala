package de.proteinevolution.models.forms

import de.proteinevolution.models.database.jobs.JobState
import io.circe.generic.JsonCodec

@JsonCodec case class JobForm(
    jobID: String,
    status: JobState,
    tool: String,
    code: String,
    dateCreated: Long,
    views: Seq[String],
    paramValues: Map[String, String]
)
