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

package de.proteinevolution.backend.dao

import de.proteinevolution.statistics.StatisticsObject
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
