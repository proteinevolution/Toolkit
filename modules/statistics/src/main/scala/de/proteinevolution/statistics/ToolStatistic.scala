/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.statistics

import de.proteinevolution.common.models.database.jobs.JobState
import de.proteinevolution.common.models.database.jobs.JobState._
import io.circe.Encoder
import io.circe.generic.semiauto._

case class ToolStatistic(
    toolName: String,
    var count: Int = 0,
    var failedCount: Int = 0,
    var deletedCount: Int = 0,
    var internalCount: Int = 0
) {

  def addJobEventLog(jobEventLog: JobEventLog): Unit = {
    val jobStates: List[JobState] = jobEventLog.events.map(jobEvent => jobEvent.jobState)
    count += 1
    if (jobStates.contains(Error)) {
      failedCount += 1
    }
    if (jobStates.contains(Deleted)) {
      deletedCount += 1
    }
    if (jobEventLog.internalJob) {
      internalCount += 1
    }
  }
}

object ToolStatistic {
  implicit val toolStatsEncoder: Encoder[ToolStatistic] = deriveEncoder[ToolStatistic]
}
