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

package de.proteinevolution.jobs.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ JobStateChanged, SetSGEID }
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.{ Done, Error, Queued, Running }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import better.files._

@Singleton
class ClusterApiController @Inject()(constants: ConstantsV2, jobActorAccess: JobActorAccess, cc: ControllerComponents)
    extends ToolkitController(cc) {

  def setJobStatus(status: String, jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      val jobStatus = status match {
        case "done"    => Done
        case "error"   => Error
        case "queued"  => Queued
        case "running" => Running
      }
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, jobStatus))
      NoContent
    } else BadRequest("Permission denied")
  }

  def setSgeId(jobID: String, sgeID: String, key: String): Action[AnyContent] = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, SetSGEID(jobID, sgeID))
      NoContent
    } else BadRequest("Permission denied")
  }

  private def checkKey(jobID: String, key: String): Boolean = {
    (for {
      in <- File(constants.jobPath + "/" + jobID + "/key").newInputStream.autoClosed
    } yield {
      in.lines.mkString.replaceAll("\n", "") == key
    }).get()
  }

}
