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

package de.proteinevolution.backend.dao

import de.proteinevolution.statistics.{ StatisticsObject, UserStatistic }
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class BackendDao @Inject(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private[backend] lazy val statisticsCol: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection[BSONCollection]("statistics"))

  def getStats: Future[StatisticsObject] = {
    statisticsCol
      .map(_.find(BSONDocument(), Option.empty[BSONDocument]))
      .flatMap(_.one[StatisticsObject])
      .map(_.getOrElse(StatisticsObject()))
  }

  def updateStats(statisticsObject: StatisticsObject): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(
        BSONDocument(StatisticsObject.ID -> statisticsObject.statisticsID),
        statisticsObject,
        fetchNewObject = true,
        upsert = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      ).map(_.result[StatisticsObject])
    )
  }

  def setStatsCurrentDeleted(
      statisticsObject: StatisticsObject,
      currentDeleted: Int
  ): Future[Option[StatisticsObject]] = {
    statisticsCol.flatMap(
      _.findAndUpdate(
        selector = BSONDocument(StatisticsObject.ID -> statisticsObject.statisticsID),
        update = BSONDocument(
          "$set" -> BSONDocument(
            s"${StatisticsObject.USERSTATISTICS}.${UserStatistic.CURRENTDELETED}" -> currentDeleted
          )
        ),
        fetchNewObject = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        upsert = false,
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      ).map(_.result[StatisticsObject])
    )
  }

  lazy val loadStatisticsCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("loadStatistics"))
  }

}
