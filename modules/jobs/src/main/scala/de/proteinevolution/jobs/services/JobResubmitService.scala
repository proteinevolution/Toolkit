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

import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.ResubmitData
import javax.inject.{ Inject, Singleton }
import play.api.Logging

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobResubmitService @Inject()(constants: ConstantsV2, jobDao: JobDao)(implicit ec: ExecutionContext)
    extends ToolkitTypes
    with Logging {

  def checkJobID(newJobId: String): Future[ResubmitData] = {
    jobDao.findJob(newJobId).flatMap {
      case Some(_) => generateJobIDSuggestion(newJobId)
      case None    => Future.successful(ResubmitData(exists = false, None))
    }
  }

  private def generateJobIDSuggestion(existJobID: String): Future[ResubmitData] = {
    val nextTry: String = existJobID match {
      case constants.jobIDPattern(start, v) => s"$start${constants.jobIDVersioningCharacter}${v.toInt + 1}"
      case _                                => s"$existJobID${constants.jobIDVersioningCharacter}1"
    }
    jobDao.findJob(nextTry).flatMap {
      case Some(_) => generateJobIDSuggestion(nextTry)
      case None    => Future.successful(ResubmitData(exists = true, Some(nextTry)))
    }
  }
}
