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

import java.time.temporal.IsoFields

import de.proteinevolution.common.models.database.jobs.JobState.Submitted
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}

case class StatisticsObject(
    totalToolCollection: ToolCollectionStatistic = ToolCollectionStatistic(),
    monthlyToolCollection: Map[(Int, Int), ToolCollectionStatistic] = Map((2021, 2) -> ToolCollectionStatistic()),
    weeklyToolCollection: Map[(Int, Int), ToolCollectionStatistic] =
      Map((2021, 2) -> ToolCollectionStatistic(), (2021, 3) -> ToolCollectionStatistic())
) {

  def addJobEventLog(jobEventLog: JobEventLog): Unit = {
    totalToolCollection.addJobEventLog(jobEventLog)

    // check if jobEventLog falls into one of the weekly or monthly collections
    jobEventLog.events
      .filter(jobEvent => jobEvent.jobState == Submitted)
      .foreach(jobEvent =>
        jobEvent.timestamp match {
          case Some(timestamp) =>
            val week          = timestamp.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
            val weekBasedYear = timestamp.get(IsoFields.WEEK_BASED_YEAR)
            val year          = timestamp.getYear
            val month         = timestamp.getMonthValue

            if (monthlyToolCollection.contains((year, month))) {
              monthlyToolCollection((year, month)).addJobEventLog(jobEventLog)
            }
            if (weeklyToolCollection.contains((weekBasedYear, week))) {
              weeklyToolCollection((weekBasedYear, week)).addJobEventLog(jobEventLog)
            }

          case None =>
        }
      )
  }

  case class DatedToolCollectionList(
      year: Int,
      month: Int = 0,
      week: Int = 0,
      toolCollection: ToolCollectionStatistic
  ) {}

  object DatedToolCollectionList {

    implicit val toolStatsEncoder: Encoder[DatedToolCollectionList] = deriveEncoder[DatedToolCollectionList]
  }

  def monthlyToolCollectionList(): List[DatedToolCollectionList] = {
    monthlyToolCollection.toList.map(entry =>
      DatedToolCollectionList(
        entry._1._1,
        month = entry._1._2,
        toolCollection = entry._2
      )
    )
  }
  def weeklyToolCollectionList(): List[DatedToolCollectionList] = {
    weeklyToolCollection.toList.map(entry =>
      DatedToolCollectionList(
        entry._1._1,
        week = entry._1._2,
        toolCollection = entry._2
      )
    )
  }

}

object StatisticsObject {

  val TOTALTOOLSTATISTICS   = "totalToolStat"
  val MONTHLYTOOLSTATISTICS = "monthlyToolStat"
  val WEEKLYTOOLSTATISTICS  = "weeklyToolStat"

  implicit val toolCollectionEncoder: Encoder[StatisticsObject] = (obj: StatisticsObject) =>
    Json.obj(
      (TOTALTOOLSTATISTICS, obj.totalToolCollection.asJson),
      (MONTHLYTOOLSTATISTICS, obj.monthlyToolCollectionList().asJson),
      (WEEKLYTOOLSTATISTICS, obj.weeklyToolCollectionList().asJson)
    )

}
