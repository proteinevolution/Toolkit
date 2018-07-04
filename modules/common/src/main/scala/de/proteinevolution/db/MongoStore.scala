package de.proteinevolution.db

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.models.database.statistics.StatisticsObject
import de.proteinevolution.models.database.users.User
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class MongoStore @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext)
    extends ReactiveMongoComponents {

  /*
   *                Complete Statistics collection
   */
  /**
   * Basic access to the statistics collection
   */
  lazy val statisticsCol: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  /**
   * Returns the (only) statistic object in the database
   * @return
   */
  def getStats: Future[StatisticsObject] = {
    statisticsCol.map(_.find(BSONDocument())).flatMap(_.one[StatisticsObject]).map(_.getOrElse(StatisticsObject()))
  }

  /**
   * Updates / inserts and returns the statistic object
   * @param statisticsObject statistic object to update
   * @return
   */
  def updateStats(statisticsObject: StatisticsObject): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = statisticsObject,
                      upsert = true,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  /**
   * Modifies the statistics object
   * @param statisticsObject
   * @param modifier
   * @return
   */
  def modifyStats(statisticsObject: StatisticsObject, modifier: BSONDocument): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = modifier,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  /*
   *                Cluster load statistics
   */

  /**
   * Basic access to the load statistics collection
   * @return
   */
  lazy val loadStatisticsCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))
  }

}
