package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import de.proteinevolution.models.util.ZonedDateTimeHelper
import play.api.libs.json._
import reactivemongo.bson._

case class StatisticsObject(
    statisticsID: BSONObjectID = BSONObjectID.generate(),
    userStatistics: UserStatistic = UserStatistic(),
    toolStatistics: List[ToolStatistic] = List.empty[ToolStatistic],
    datePushed: List[ZonedDateTime] = List.empty[ZonedDateTime]
) {

  /**
   * Returns the tool Statistic elements as a map
   * @return
   */
  def getToolStatisticMap: Map[String, ToolStatistic] = {
    toolStatistics.map(toolStatistic => (toolStatistic.toolName, toolStatistic)).toMap
  }

  /**
   * Creates new and empty tool statistic elements with the provided name list
   * @param toolNames
   * @return
   */
  def updateTools(toolNames: List[String]): StatisticsObject = {
    this.copy(
      toolStatistics = toolNames.map(
        toolName =>
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
   * @param jobEventLogs
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
    val monthsInInterval = for (extraMonths <- 0 to totalMonths)
      yield beginDate.plusMonths(extraMonths.toLong).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)

    // Group the job events by tool
    val jobEventsGroupedByTool = jobEventLogs.groupBy(_.toolName)

    // Return a new instance of this object with the updated data
    this.copy(
      toolStatistics = {
        // map over all tool statistics
        this.toolStatistics.map {
          toolStatistic =>
            // check if there are any elements available for this tool
            jobEventsGroupedByTool.get(toolStatistic.toolName) match {
              case Some(jobEventLogsForMonths) =>
                // since there are elements from this tool, group them by month
                val jobEventsInMonths =
                  jobEventLogsForMonths.groupBy(_.dateCreated.truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1))
                // iterate over all months
                val counts = monthsInInterval.map {
                  startOfMonth =>
                    // check if the month is in the group
                    jobEventsInMonths.get(startOfMonth) match {
                      case Some(jobEventLogsForMonth) =>
                        // Found events within this month.
                        (jobEventLogsForMonth.length,
                         jobEventLogsForMonth.count(_.hasFailed),
                         jobEventLogsForMonth.count(_.isDeleted),
                         jobEventLogsForMonth.count(_.internalJob))
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
   * @return
   */
  def lastPushed: ZonedDateTime = {
    datePushed.headOption match {
      case Some(_) => datePushed.max[ZonedDateTime](Ordering.fromLessThan(_ isBefore _)).truncatedTo(ChronoUnit.DAYS)
      case None    => ZonedDateTime.parse("2017-02-01T00:00:00.000+02:00")
    }
  }
}

object StatisticsObject {
  val ID             = "statisticsID"
  val IDDB           = "_id"
  val USERSTATISTICS = "userStat"
  val TOOLSTATISTICS = "toolStat"
  val DATEPUSHED     = "datePushed"

  implicit object JsonWriter extends Writes[StatisticsObject] {
    override def writes(statisticObject: StatisticsObject): JsObject = Json.obj(
      IDDB           -> statisticObject.statisticsID.stringify,
      USERSTATISTICS -> statisticObject.userStatistics,
      TOOLSTATISTICS -> statisticObject.toolStatistics,
      DATEPUSHED     -> statisticObject.datePushed
    )
  }

  implicit object Reader extends BSONDocumentReader[StatisticsObject] {
    def read(bson: BSONDocument): StatisticsObject = {
      StatisticsObject(
        statisticsID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        userStatistics = bson.getAs[UserStatistic](USERSTATISTICS).getOrElse(UserStatistic()),
        toolStatistics = bson.getAs[List[ToolStatistic]](TOOLSTATISTICS).getOrElse(List.empty),
        datePushed = bson
          .getAs[List[BSONDateTime]](DATEPUSHED)
          .getOrElse(List.empty[BSONDateTime])
          .map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[StatisticsObject] {
    def write(statisticObject: StatisticsObject): BSONDocument = BSONDocument(
      IDDB           -> statisticObject.statisticsID,
      USERSTATISTICS -> statisticObject.userStatistics,
      TOOLSTATISTICS -> statisticObject.toolStatistics,
      DATEPUSHED     -> statisticObject.datePushed.map(a => BSONDateTime(a.toInstant.toEpochMilli))
    )
  }
}
