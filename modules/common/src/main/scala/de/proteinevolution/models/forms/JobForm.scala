package de.proteinevolution.models.forms

import de.proteinevolution.models.database.jobs.JobState

// Server returns such an object when asked for a job
case class JobForm(jobID: String,
    state: JobState,
    dateCreated: String,
    toolitem: ToolForm,
    views: Seq[String],
    paramValues: Map[String, String])