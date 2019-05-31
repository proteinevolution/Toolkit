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

package de.proteinevolution.results.services

import de.proteinevolution.common.models.ToolName
import de.proteinevolution.jobs.dao.JobDao
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[results] class ToolNameGetService @Inject()(jobDao: JobDao)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = jobDao.findJob(jobID).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
