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

package de.proteinevolution.jobs.services

import java.time.ZonedDateTime

import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.statistics.{ JobEvent, JobEventLog }
import javax.inject.{ Inject, Singleton }
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobFrontendToolsService @Inject()(
    jobIdProvider: JobIdProvider,
    jobDao: JobDao
)(implicit ec: ExecutionContext) {

  import cats.effect.unsafe.implicits.global

  def logFrontendJob(toolName: String): Future[WriteResult] = {
    for {
      jobId <- jobIdProvider.runSafe.unsafeToFuture()
      log = generateJobEventLog(jobId, toolName)
      wr <- jobDao.addJobLog(log)
    } yield wr
  }

  private def generateJobEventLog(jobId: String, toolName: String): JobEventLog = {
    JobEventLog(
      jobID = jobId,
      toolName = toolName.trim.toLowerCase,
      events = JobEvent(Done, Some(ZonedDateTime.now)) :: Nil
    )
  }

}
