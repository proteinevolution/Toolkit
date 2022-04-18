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

import io.circe.Encoder
import io.circe.generic.semiauto._
import reactivemongo.api.bson._

case class UserStatistic(
    currentDeleted: Int = 0,
    monthly: List[Int] = List.empty[Int],
    monthlyLoggedIn: List[Int] = List.empty[Int],
    monthlyInternal: List[Int] = List.empty[Int],
    monthlyUniqueIP: List[Int] = List.empty[Int],
    monthlyDeleted: List[Int] = List.empty[Int]
) {

  def total: Long = monthly.map(_.toLong).sum[Long]

  def totalFailed: Long = monthlyLoggedIn.map(_.toLong).sum[Long]

}

object UserStatistic {

  final val CURRENTDELETED = "currentDeleted"

  implicit val userStatsEncoder: Encoder[UserStatistic] = deriveEncoder[UserStatistic]

  implicit val bsonHandler: BSONDocumentHandler[UserStatistic] = Macros.handler[UserStatistic]

}
