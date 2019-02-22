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

package de.proteinevolution.search.services

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.base.helpers.ToolkitTypes._
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.tools.{ Tool, ToolConfig }
import de.proteinevolution.user.User
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SearchService @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    toolConfig: ToolConfig
)(implicit ec: ExecutionContext)
    extends JobFolderValidation {

  def recentJob(user: User): Future[Option[Job]] = jobDao.findSortedJob(user.userID)

  def autoComplete(user: User, queryString_ : String): OptionT[Future, List[Job]] = {
    val queryString = queryString_.trim()
    val tools: List[Tool] = toolConfig.values.values
      .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
      .filterNot(_.toolNameShort == "hhpred_manual")
      .toList
    if (tools.isEmpty) {
      (for {
        jobs     <- OptionT.liftF(jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))))
        filtered <- OptionT.pure[Future](jobs.filter(job => job.ownerID.contains(user.userID)))
      } yield filtered).flatMapF { jobs =>
        if (jobs.isEmpty) {
          OptionT(jobDao.findJob(queryString)).filter(job => resultsExist(job.jobID, constants)).map(_ :: Nil).value
        } else {
          fuccess(Some(jobs.filter(job => resultsExist(job.jobID, constants))))
        }
      }
    } else {
      OptionT.liftF(
        jobDao.findJobs(
          BSONDocument(Job.OWNERID -> user.userID, Job.TOOL -> BSONDocument("$in" -> tools.map(_.toolNameShort)))
        )
      )
    }
  }

}
