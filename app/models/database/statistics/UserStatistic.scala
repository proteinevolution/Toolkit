package models.database.statistics

import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 14.07.17.
  */
case class UserStatistic(statisticsID: BSONObjectID,
                         monthly: List[Int],
                         monthlyLoggedIn: List[Int],
                         monthlyInternal: List[Int],
                         monthlyUniqueIP: List[Int],
                         monthlyDeleted : List[Int]) {
  def total: Long       = monthly.map(_.toLong).sum[Long]
  def totalFailed: Long = monthlyLoggedIn.map(_.toLong).sum[Long]
}

object UserStatistic {
  val ID              = "statisticsID"
  val IDDB            = "_id"
  val MONTHLY         = "monthly"
  val MONTHLYLOGGEDIN = "monthlyLoggedIn"
  val MONTHLYINTERNAL = "monthlyInternal"
  val MONTHLYUNIQUEIP = "monthlyUniqueIP"
  val MONTHLYDELETED  = "monthlyDeleted"

  implicit object JsonWriter extends Writes[UserStatistic] {
    override def writes(toolStatistic: UserStatistic): JsObject = Json.obj(
      IDDB            -> toolStatistic.statisticsID.stringify,
      MONTHLY         -> toolStatistic.monthly,
      MONTHLYLOGGEDIN -> toolStatistic.monthlyLoggedIn,
      MONTHLYINTERNAL -> toolStatistic.monthlyInternal,
      MONTHLYUNIQUEIP -> toolStatistic.monthlyUniqueIP,
      MONTHLYDELETED  -> toolStatistic.monthlyDeleted
    )
  }

  implicit object Reader extends BSONDocumentReader[UserStatistic] {
    def read(bson: BSONDocument): UserStatistic = {
      UserStatistic(
        statisticsID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        monthly = bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty),
        monthlyLoggedIn = bson.getAs[List[Int]](MONTHLYLOGGEDIN).getOrElse(List.empty),
        monthlyInternal = bson.getAs[List[Int]](MONTHLYINTERNAL).getOrElse(List.empty),
        monthlyUniqueIP = bson.getAs[List[Int]](MONTHLYUNIQUEIP).getOrElse(List.empty),
        monthlyDeleted  = bson.getAs[List[Int]](MONTHLYDELETED).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[UserStatistic] {
    def write(toolStatistic: UserStatistic): BSONDocument = BSONDocument(
      IDDB            -> toolStatistic.statisticsID,
      MONTHLY         -> toolStatistic.monthly,
      MONTHLYLOGGEDIN -> toolStatistic.monthlyLoggedIn,
      MONTHLYINTERNAL -> toolStatistic.monthlyInternal,
      MONTHLYUNIQUEIP -> toolStatistic.monthlyUniqueIP,
      MONTHLYDELETED  -> toolStatistic.monthlyDeleted
    )
  }
}
