package de.proteinevolution.backend.dao

import de.proteinevolution.common.models.database.statistics.StatisticsObject
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class BackendDao @Inject()(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private[backend] lazy val statisticsCol: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  def getStats: Future[StatisticsObject] = {
    statisticsCol
      .map(_.find(BSONDocument(), None))
      .flatMap(_.one[StatisticsObject])
      .map(_.getOrElse(StatisticsObject()))
  }

  def updateStats(statisticsObject: StatisticsObject): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = statisticsObject,
                      upsert = true,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  def modifyStats(statisticsObject: StatisticsObject, modifier: BSONDocument): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(selector = BSONDocument(StatisticsObject.IDDB -> statisticsObject.statisticsID),
                      update = modifier,
                      fetchNewObject = true).map(_.result[StatisticsObject])
    )
  }

  lazy val loadStatisticsCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))
  }

}
