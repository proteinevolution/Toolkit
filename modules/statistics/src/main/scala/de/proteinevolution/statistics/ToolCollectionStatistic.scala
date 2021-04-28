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

import io.circe.syntax._
import io.circe.{Encoder, Json}

case class ToolCollectionStatistic(
    var toolStatistics: Map[String, ToolStatistic] = Map()
) {

  def addJobEventLog(jobEventLog: JobEventLog): Unit = {
    // check if toolStatistic exists
    if (!toolStatistics.contains(jobEventLog.toolName)) {
      toolStatistics += (jobEventLog.toolName -> ToolStatistic(jobEventLog.toolName))
    }
    toolStatistics(jobEventLog.toolName).addJobEventLog(jobEventLog)
  }
}

  object ToolCollectionStatistic {

  val SUMMARY = "summary"
  val TOOLSTATISTICS = "toolStats"

  implicit val toolCollectionEncoder : Encoder[ToolCollectionStatistic] = (obj: ToolCollectionStatistic) =>
    Json.obj(
      (TOOLSTATISTICS, obj.toolStatistics.asJson)
    )

}
