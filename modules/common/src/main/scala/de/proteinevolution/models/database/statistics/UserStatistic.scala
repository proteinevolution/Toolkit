package de.proteinevolution.models.database.statistics

import play.api.libs.json._
import reactivemongo.bson._

case class UserStatistic(
    currentDeleted: Int = 0,
    monthly: List[Int] = List.empty[Int],
    monthlyLoggedIn: List[Int] = List.empty[Int],
    monthlyInternal: List[Int] = List.empty[Int],
    monthlyUniqueIP: List[Int] = List.empty[Int],
    monthlyDeleted: List[Int] = List.empty[Int]
) {
  def total: Long       = monthly.map(_.toLong).sum[Long]
  def totalFailed: Long = monthlyLoggedIn.map(_.toLong).sum[Long]
}

object UserStatistic {
  val CURRENTDELETED  = "currentDeleted"
  val MONTHLY         = "monthly"
  val MONTHLYLOGGEDIN = "monthlyLoggedIn"
  val MONTHLYINTERNAL = "monthlyInternal"
  val MONTHLYUNIQUEIP = "monthlyUniqueIP"
  val MONTHLYDELETED  = "monthlyDeleted"

  implicit object JsonWriter extends Writes[UserStatistic] {
    override def writes(userStatistic: UserStatistic): JsObject = Json.obj(
      CURRENTDELETED  -> userStatistic.currentDeleted,
      MONTHLY         -> userStatistic.monthly,
      MONTHLYLOGGEDIN -> userStatistic.monthlyLoggedIn,
      MONTHLYINTERNAL -> userStatistic.monthlyInternal,
      MONTHLYUNIQUEIP -> userStatistic.monthlyUniqueIP,
      MONTHLYDELETED  -> userStatistic.monthlyDeleted
    )
  }

  implicit object Reader extends BSONDocumentReader[UserStatistic] {
    def read(bson: BSONDocument): UserStatistic = {
      UserStatistic(
        currentDeleted = bson.getAs[Int](CURRENTDELETED).getOrElse(0),
        monthly = bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty),
        monthlyLoggedIn = bson.getAs[List[Int]](MONTHLYLOGGEDIN).getOrElse(List.empty),
        monthlyInternal = bson.getAs[List[Int]](MONTHLYINTERNAL).getOrElse(List.empty),
        monthlyUniqueIP = bson.getAs[List[Int]](MONTHLYUNIQUEIP).getOrElse(List.empty),
        monthlyDeleted = bson.getAs[List[Int]](MONTHLYDELETED).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[UserStatistic] {
    def write(userStatistic: UserStatistic): BSONDocument = BSONDocument(
      CURRENTDELETED  -> userStatistic.currentDeleted,
      MONTHLY         -> userStatistic.monthly,
      MONTHLYLOGGEDIN -> userStatistic.monthlyLoggedIn,
      MONTHLYINTERNAL -> userStatistic.monthlyInternal,
      MONTHLYUNIQUEIP -> userStatistic.monthlyUniqueIP,
      MONTHLYDELETED  -> userStatistic.monthlyDeleted
    )
  }
}
