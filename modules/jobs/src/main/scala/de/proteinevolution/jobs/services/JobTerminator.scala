/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

package de.proteinevolution.jobs.services

import java.time.{ Duration, ZonedDateTime }

import de.proteinevolution.cluster.api.QStat
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.common.models.database.jobs.JobState

trait JobTerminator {

  // checks whether the jobs has exceeded the hardruntime and should be killed by the SGE by now
  def isOverDue(job: Job): Boolean = {
    val jobIsDead = job.clusterData.map { cd =>
      val startDate = cd.dateStarted.getOrElse(ZonedDateTime.now())
      val runtime   = cd.hardruntime.getOrElse(0).toLong
      val isRunning = job.status == JobState.Running && cd.dateFinished.isEmpty
      val duration  = Duration.between(startDate, ZonedDateTime.now()).getSeconds
      duration > runtime && isRunning
    }
    jobIsDead.getOrElse(false)
  }

  def sgeFailed(sgeId: String, qStat: QStat): Boolean = {
    qStat.qStatJobs.find(sgeJob => sgeJob.sgeID == sgeId).exists(qStat => qStat.hasFailed || qStat.badRunscript)
  }

}
