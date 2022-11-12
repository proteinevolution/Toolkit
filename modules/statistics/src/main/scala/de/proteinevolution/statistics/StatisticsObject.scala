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

package de.proteinevolution.statistics

import de.proteinevolution.common.models.database.jobs.JobState.Submitted
import io.circe.syntax.EncoderOps
import io.circe.{ Encoder, Json }

import java.time.LocalDate
import java.time.temporal.IsoFields

case class StatisticsObject(
    fromTime: LocalDate,
    toTime: LocalDate
) {

  val totalToolCollection: ToolStatisticCollection                    = ToolStatisticCollection()
  var monthlyToolCollection: Map[(Int, Int), ToolStatisticCollection] = Map()
  var weeklyToolCollection: Map[(Int, Int), ToolStatisticCollection]  = Map()

  // fill monthlyToolCollection with empty ToolCollectionStatistics
  var year: Int    = fromTime.getYear
  var month: Int   = fromTime.getMonthValue
  var toYear: Int  = toTime.getYear
  var toMonth: Int = toTime.getMonthValue
  while (year < toYear || (year == toYear && month <= toMonth)) {
    monthlyToolCollection += (year, month) -> ToolStatisticCollection()
    if (month == 12) {
      month = 1
      year += 1
    } else {
      month += 1
    }
  }
  // fill weeklyToolCollection with empty ToolCollectionStatistics
  year = fromTime.get(IsoFields.WEEK_BASED_YEAR)
  var week: Int = fromTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
  toYear = toTime.get(IsoFields.WEEK_BASED_YEAR)
  var toWeek: Int = toTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
  while (year < toYear || (year == toYear && week <= toWeek)) {
    weeklyToolCollection += (year, week) -> ToolStatisticCollection()
    val weekOfDecember31: Int = LocalDate.of(year, 12, 31).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val weeksInYear: Int      = if (weekOfDecember31 == 1) 52 else weekOfDecember31
    if (week == weeksInYear) {
      week = 1
      year += 1
    } else {
      week += 1
    }
  }

  def addJobEventLog(jobEventLog: JobEventLog): Unit = {
    val submitEvent: Option[JobEvent] = jobEventLog.events.find(jobEvent => jobEvent.jobState == Submitted)
    // check if submit event with timestamp exists
    submitEvent.flatMap(_.timestamp).foreach { timestamp =>
      // check if jobEventLog falls into the min max time range
      val submitDate: LocalDate = timestamp.toLocalDate
      if (!submitDate.isBefore(fromTime) && !submitDate.isAfter(toTime)) {
        totalToolCollection.addJobEventLog(jobEventLog)
      }

      // check if jobEventLog falls into weekly or monthly statistic
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
    }
  }

  def monthlyToolCollectionList(): List[MonthlyToolStats] = {
    monthlyToolCollection.toList
      .map(entry =>
        MonthlyToolStats(
          year = entry._1._1,
          month = entry._1._2,
          toolStats = entry._2
        )
      )
      .sortBy(weeklyToolStats => weeklyToolStats.month)
      .sortBy(weeklyToolStats => weeklyToolStats.year)
  }

  def weeklyToolCollectionList(): List[WeeklyToolStats] = {
    weeklyToolCollection.toList
      .map(entry =>
        WeeklyToolStats(
          year = entry._1._1,
          week = entry._1._2,
          toolStats = entry._2
        )
      )
      .sortBy(weeklyToolStats => weeklyToolStats.week)
      .sortBy(weeklyToolStats => weeklyToolStats.year)
  }

}

object StatisticsObject {

  val TOTALTOOLSTATISTICS   = "totalToolStats"
  val MONTHLYTOOLSTATISTICS = "monthlyToolStats"
  val WEEKLYTOOLSTATISTICS  = "weeklyToolStats"

  implicit val toolCollectionEncoder: Encoder[StatisticsObject] = (obj: StatisticsObject) =>
    Json.obj(
      (TOTALTOOLSTATISTICS, obj.totalToolCollection.asJson),
      (MONTHLYTOOLSTATISTICS, obj.monthlyToolCollectionList().asJson),
      (WEEKLYTOOLSTATISTICS, obj.weeklyToolCollectionList().asJson)
    )
}
