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

  /*
   *                User collection access
   */

  /**
   * Basic access to the user collection
   * @return
   */
  lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  /**
   * Inserts a user to the collection
   * @param user
   * @return
   */
  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(user))

  /**
   * Finds a user in the collection
   * @param selector
   * @return
   */
  def findUser(selector: BSONDocument): Future[Option[User]] =
    userCollection.flatMap(_.find(selector).one[User])

  /**
   * Returns multiple users from the collection
   * @param selector
   * @return
   */
  def findUsers(selector: BSONDocument): Future[scala.List[User]] = {
    userCollection.map(_.find(selector).cursor[User]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))
  }

  /**
   * Modifies and returns the changed user
   * @param selector
   * @param modifier
   * @return
   */
  def modifyUser(selector: BSONDocument, modifier: BSONDocument): Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }

  /**
   * Modifies multiple users
   * @param selector
   * @param modifier
   * @return
   */
  def modifyUsers(selector: BSONDocument, modifier: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  /**
   * Removes users with the matching selector
   * @param selector
   * @return
   */
  def removeUsers(selector: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.delete().one(selector))
  }

  /**
   * Overwrites or inserts a User
   * @param user
   * @return
   */
  def upsertUser(user: User): Future[Option[User]] = {
    userCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(User.IDDB -> user.userID),
                      update = user,
                      upsert = true,
                      fetchNewObject = true).map(_.result[User])
    )
  }
}
