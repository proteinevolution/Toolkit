package de.proteinevolution.common.models.database.statistics

import io.circe.Encoder
import io.circe.generic.semiauto._
import reactivemongo.bson._

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

  implicit val bsonHandler: BSONHandler[BSONDocument, UserStatistic] = Macros.handler[UserStatistic]

}
