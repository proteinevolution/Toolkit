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

import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.tel.env.Env
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobHashService @Inject()(
    env: Env,
    jobDao: JobDao,
    constants: ConstantsV2,
    hashService: GeneralHashService
)(implicit ec: ExecutionContext)
    extends JobFolderValidation {

  def checkHash(jobID: String): OptionT[Future, Job] = {
    for {
      job      <- OptionT(jobDao.findJob(jobID))
      list     <- OptionT.liftF(listSameJobsSorted(job))
      filtered <- OptionT.fromOption[Future](list.find(filterJobs(job, _)))
    } yield filtered
  }

  private[this] def listSameJobsSorted(job: Job): Future[List[Job]] =
    jobDao.findAndSortJobs(hashService.generateJobHash(job, params(job.jobID), env))

  private[this] def filterJobs(job: Job, j: Job): Boolean =
    (j.isPublic || j.ownerID == job.ownerID) && j.status == Done && resultsExist(j.jobID, constants)

  private[this] def params(jobID: String): Map[String, String] = {
    (constants.jobPath / jobID / constants.serializedParam).readDeserialized[Map[String, String]]()
  }

}
