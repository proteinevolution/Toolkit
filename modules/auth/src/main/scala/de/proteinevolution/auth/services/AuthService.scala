package de.proteinevolution.auth.services

import akka.actor.ActorRef
import cats.data.{ EitherT, OptionT }
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.{ AuthError, AuthResponse, FormDefinitions }
import de.proteinevolution.models.database.users.User.Login
import de.proteinevolution.models.database.users.{ User, UserToken }
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

@Singleton
class AuthService @Inject()(userSessions: UserSessions, userDao: UserDao) {

  def signInSubmit(signInFormUser: Login): EitherT[Future, AuthError, AuthResponse] = {

    val futureUser = userDao.findUser(
      BSONDocument(
        "$or" -> List(
          BSONDocument(User.EMAIL     -> signInFormUser.nameLogin),
          BSONDocument(User.NAMELOGIN -> signInFormUser.nameLogin)
        )
      )
    )

    (for {
      databaseUser <- OptionT(futureUser)
      if databaseUser.checkPassword(signInFormUser.password) && databaseUser.accountType > 0
    } yield {
      databaseUser
    }).flatMapF {
      databaseUser =>
    }

  }

  /* val futureUser = mongoStore.findUser(
                BSONDocument(
                  "$or" -> List(BSONDocument(User.EMAIL -> signInFormUser.nameLogin),
                    BSONDocument(User.NAMELOGIN -> signInFormUser.nameLogin))
                )

              futureUser.flatMap {
                case Some(databaseUser) =>
                  // Check the password
                  if (databaseUser.checkPassword(signInFormUser.password) && databaseUser.accountType > 0) {
                    // create a modifier document to change the last login date in the Database
                    val selector = BSONDocument(User.IDDB -> databaseUser.userID)
                    // Change the login time and give the new Session ID to the user.
                    // Additionally add the watched jobs to the users watchlist.
                    val modifier = userSessions.getUserModifier(databaseUser, forceSessionID = true)
                    // TODO this adds the non logged in user's jobs to the now logged in user's job list
                    //                            "$addToSet"        ->
                    //               BSONDocument(User.JOBS          ->
                    //               BSONDocument("$each"            -> unregisteredUser.jobs)))
                    // Finally add the edits to the collection
                    userSessions.modifyUserWithCache(selector, modifier).map {
                      case Some(loggedInUser) =>
                        logger.info(
                          "\n-[old user]-\n"
                              + unregisteredUser.toString
                              + "\n-[new user]-\n"
                              + loggedInUser.toString
                        )
                        // Remove the old, not logged in user
                        //removeUser(BSONDocument(User.IDDB -> unregisteredUser.userID))
                        userSessions.removeUserFromCache(unregisteredUser)

                        // Tell the job actors to copy all jobs connected to the old user to the new user
                        wsActorCache.get[List[ActorRef]](unregisteredUser.userID.stringify) match {
                          case Some(wsActors) =>
                            val actorList: List[ActorRef] = wsActors: List[ActorRef]
                            wsActorCache.set(loggedInUser.userID.stringify, actorList)
                            actorList.foreach(_ ! ChangeSessionID(loggedInUser.sessionID.get))
                            wsActorCache.remove(unregisteredUser.userID.stringify)
                          case None =>

      }
    } */

}
