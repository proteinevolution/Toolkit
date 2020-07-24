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

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

import de.proteinevolution.common.models.util.{ ZonedDateTimeHelper => helper }
import io.circe.syntax._
import io.circe.{ Encoder, Json }
import reactivemongo.api.bson._

import scala.util.{ Success, Try }

case class StatisticsObject(
    statisticsID: String = UUID.randomUUID().toString,
    userStatistics: UserStatistic = UserStatistic(),
    toolStatistics: List[ToolStatistic] = List.empty[ToolStatistic],
    datePushed: List[ZonedDateTime] = List.empty[ZonedDateTime]
) {

  /**
   * Returns the tool Statistic elements as a map
   *
   * @return
   */
  def getToolStatisticMap: Map[String, ToolStatistic] = {
    toolStatistics.map(toolStatistic => (toolStatistic.toolName, toolStatistic)).toMap
  }

  /**
   * Creates new and empty tool statistic elements with the provided name list
   * @return
   */
  def updateTools(toolNames: List[String]): StatisticsObject = {
    this.copy(
      toolStatistics = toolNames.map(toolName =>
        this.toolStatistics
          .find(_.toolName == toolName)
          .getOrElse(
            ToolStatistic(
              toolName,
              List.fill[Int](this.datePushed.length)(0),
              List.fill[Int](this.datePushed.length)(0),
              List.fill[Int](this.datePushed.length)(0),
              List.fill[Int](this.datePushed.length)(0)
            )
          )
      )
    )
  }

  /**
   * Adds the job events within the begin and end date to the tool statistics
   * @return
   */
  def addMonthsToTools(
      jobEventLogs: List[JobEventLog],
      beginDate: ZonedDateTime,
      endDate: ZonedDateTime
  ): StatisticsObject = {
    // Get the total amount of months in between the two given dates (expecting the first moment of the months here)
    val totalMonths: Int = beginDate.until(endDate, ChronoUnit.MONTHS).toInt

    // Get all months in between the two dates
    val monthsInInterval =
      for (extraMonths <- 0 to totalMonths)
        yield beginDate.plusMonths(extraMonths.toLong).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)

    // Group the job events by tool
    val jobEventsGroupedByTool = jobEventLogs.groupBy(_.toolName)

    // Return a new instance of this object with the updated data
    this.copy(
      toolStatistics = {
        // map over all tool statistics
        this.toolStatistics.map { toolStatistic =>
          // check if there are any elements available for this tool
          jobEventsGroupedByTool.get(toolStatistic.toolName) match {
            case Some(jobEventLogsForMonths) =>
              // since there are elements from this tool, group them by month
              val jobEventsInMonths =
                jobEventLogsForMonths.groupBy(_.dateCreated.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1))
              // iterate over all months
              val counts = monthsInInterval.map { startOfMonth =>
                // check if the month is in the group
                jobEventsInMonths.get(startOfMonth) match {
                  case Some(jobEventLogsForMonth) =>
                    // Found events within this month.
                    (
                      jobEventLogsForMonth.length,
                      jobEventLogsForMonth.count(_.hasFailed),
                      jobEventLogsForMonth.count(_.isDeleted),
                      jobEventLogsForMonth.count(_.internalJob)
                    )
                  case None =>
                    // Found nothing for this month.
                    (0, 0, 0, 0)
                }
              }.toList
              // add the months to the old tool statistics
              toolStatistic.addMonths(counts.map(_._1), counts.map(_._2), counts.map(_._3), counts.map(_._4))
            case None =>
              // add the empty months to the old tool statistics
              toolStatistic.addEmptyMonths(totalMonths)
          }
        }
      },
      // add the months which have been added to the list
      datePushed = this.datePushed ::: monthsInInterval.toList
    )
  }

  /**
   * Returns the date when the last push happened
   *
   * @return
   */
  def lastPushed: ZonedDateTime = {
    datePushed.headOption match {
      case Some(_) => datePushed.max[ZonedDateTime](Ordering.fromLessThan(_.isBefore(_))).truncatedTo(ChronoUnit.DAYS)
      case None    => ZonedDateTime.parse("2017-02-01T00:00:00.000+02:00")
    }
  }
}

object StatisticsObject {

  val ID             = "id"
  val USERSTATISTICS = "userStat"
  val TOOLSTATISTICS = "toolStat"
  val DATEPUSHED     = "datePushed"

  implicit val statObjEncoder: Encoder[StatisticsObject] = (obj: StatisticsObject) =>
    Json.obj(
      (ID, Json.fromString(obj.statisticsID)),
      (USERSTATISTICS, obj.userStatistics.asJson),
      (TOOLSTATISTICS, obj.toolStatistics.asJson),
      (DATEPUSHED, obj.datePushed.asJson)
    )

  implicit object Reader extends BSONDocumentReader[StatisticsObject] {
    def readDocument(bson: BSONDocument): Try[StatisticsObject] =
      Success(
        StatisticsObject(
          statisticsID = bson.getAsOpt[String](ID).getOrElse(UUID.randomUUID().toString),
          userStatistics = bson.getAsOpt[UserStatistic](USERSTATISTICS).getOrElse(UserStatistic()),
          toolStatistics = bson.getAsOpt[List[ToolStatistic]](TOOLSTATISTICS).getOrElse(List.empty),
          datePushed = bson
            .getAsOpt[List[BSONDateTime]](DATEPUSHED)
            .getOrElse(List.empty[BSONDateTime])
            .map(dt => helper.getZDT(dt))
        )
      )
  }

  implicit object Writer extends BSONDocumentWriter[StatisticsObject] {
    def writeTry(statisticObject: StatisticsObject): Try[BSONDocument] =
      Success(
        BSONDocument(
          ID             -> statisticObject.statisticsID,
          USERSTATISTICS -> statisticObject.userStatistics,
          TOOLSTATISTICS -> statisticObject.toolStatistics,
          DATEPUSHED     -> statisticObject.datePushed.map(a => BSONDateTime(a.toInstant.toEpochMilli))
        )
      )
  }

}
