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

package de.proteinevolution.auth.controllers

import java.time.ZonedDateTime

import akka.actor.ActorRef
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate._
import de.proteinevolution.auth.models.Session.ChangeSessionID
import de.proteinevolution.auth.models.{ FormDefinitions, JSONTemplate }
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tel.env.Env
import de.proteinevolution.user.{ User, UserToken }
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import play.api.{ Environment, Logging }
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AuthController @Inject()(
    userSessionService: UserSessionService,
    userDao: UserDao,
    cc: ControllerComponents,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    environment: Environment,
    env: Env,
    userAction: UserAction
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends ToolkitController(cc)
    with JSONTemplate
    with Logging {

  def signOut: Action[AnyContent] = userAction { implicit request =>
    userSessionService.removeUserFromCache(request.user)
    Ok(loggedOut()).withNewSession
  }

  def getUserData: Action[AnyContent] = userAction { implicit request =>
    logger.info("Sending user data.")
    Ok(request.user.userData.asJson)
  }

  def signInSubmit: Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    // check if unregistered user (accountType == -1)
    if (user.accountType < 0) {
      // Evaluate the Form
      FormDefinitions.signIn.bindFromRequest.fold(
        errors => {
          logger.warn(s"Errors in Login Form: ${errors.errors}")
          fuccess(Ok(loginError()))
        },
        // if no error, then insert the user to the collection
        signInFormUser => {
          userDao.findUserByUsernameOrEmail(signInFormUser.nameLogin, signInFormUser.nameLogin).flatMap {
            case Some(databaseUser) =>
              // Check the password
              if (databaseUser.checkPassword(signInFormUser.password) && databaseUser.accountType > 0) {
                // Change the login time and give the new Session ID to the user.
                // Additionally add the watched jobs to the users watchlist.
                val modifier = userSessionService.getUserModifier(databaseUser, forceSessionID = true)
                // TODO this adds the non logged in user's jobs to the now logged in user's job list
                //                            "$addToSet"        ->
                //               BSONDocument(User.JOBS          ->
                //               BSONDocument("$each"            -> user.jobs)))
                // Finally add the edits to the collection
                userSessionService.modifyUserWithCache(databaseUser.userID, modifier).map {
                  case Some(loggedInUser) =>
                    logger.info(
                      "\n-[old user]-\n"
                      + user.toString
                      + "\n-[new user]-\n"
                      + loggedInUser.toString
                    )
                    // Remove the old, not logged in user
                    //removeUser(BSONDocument(User.IDDB -> user.userID))
                    userSessionService.removeUserFromCache(user)

                    // Tell the job actors to copy all jobs connected to the old user to the new user
                    wsActorCache.get[List[ActorRef]](user.userID.stringify) match {
                      case Some(wsActors) =>
                        val actorList: List[ActorRef] = wsActors: List[ActorRef]
                        wsActorCache.set(loggedInUser.userID.stringify, actorList)
                        actorList.foreach(_ ! ChangeSessionID(loggedInUser.sessionID.get))
                        wsActorCache.remove(user.userID.stringify)
                      case None =>
                    }

                    // Everything is ok, let the user know that they are logged in now
                    Ok(loggedIn(loggedInUser)).withSession(
                      userSessionService.sessionCookie(request, loggedInUser.sessionID.get)
                    )
                  case None =>
                    Ok(loginIncorrect())
                }
              } else if (databaseUser.accountType < 1) {
                // User needs to Verify first
                fuccess(Ok(mustVerify()))
              } else {
                // Wrong Password, show the error message
                fuccess(Ok(loginIncorrect()))
              }
            case None => // no user found in database
              fuccess(Ok(loginIncorrect()))
          }
        }
      )
    } else {
      fuccess(Ok(alreadyLoggedIn()))
    }
  }

  def signUpSubmit: Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    if (user.accountType < User.NORMALUSERAWAITINGREGISTRATION) {
      // Create a new user from the information given in the form
      FormDefinitions
        .signUp(user)
        .bindFromRequest
        .fold(
          _ =>
            // Something went wrong with the Form.
            Future.successful {
              Ok(formError())
            },
          // if no error, then insert the user to the collection
          signUpFormUser => {
            if (signUpFormUser.accountType < User.NORMALUSERAWAITINGREGISTRATION) {
              // User did not accept the Terms of Service but managed to get around the JS form validation
              Future.successful {
                Ok(mustAcceptToS())
              }
            } else {
              // Check database for existing users with the same email or login name
              userDao
                .findUserByUsernameOrEmail(signUpFormUser.getUserData.nameLogin, signUpFormUser.getUserData.eMail)
                .flatMap {
                  case Some(otherUser) =>
                    if (signUpFormUser.getUserData.eMail == otherUser.getUserData.eMail) {
                      Future.successful(Ok(accountEmailUsed()))
                    } else {
                      Future.successful(Ok(accountNameUsed()))
                    }
                  case None =>
                    // Create the database entry.
                    val newUser = signUpFormUser.copy(
                      userID = BSONObjectID.generate(),
                      sessionID = None,
                      userToken = Some(
                        UserToken(
                          tokenType = UserToken.EMAIL_VERIFICATION_TOKEN,
                          eMail = Some(signUpFormUser.getUserData.eMail)
                        )
                      )
                    )
                    userDao.upsertUser(newUser).map {
                      case Some(registeredUser) =>
                        // All done. User is registered, now send the welcome eMail
                        registeredUser.userToken match {
                          case Some(token) =>
                            val eMail = NewUserWelcomeMail(registeredUser, token.token, environment, env)
                            eMail.send
                            Ok(signedUp)
                          case None => Ok(tokenMismatch())
                        }
                      case None =>
                        Ok(formError())
                    }
                }
            }
          }
        )
    } else {
      fuccess(Ok(accountNameUsed()))
    }
  }

  def resetPassword: Action[AnyContent] = Action.async { implicit request =>
    FormDefinitions.forgottenPasswordEdit.bindFromRequest.fold(
      _ =>
        Future.successful {
          Ok(formError())
        },
      // when there are no errors, then insert the user to the collection
      {
        case Some(userNameOrEmail: String) =>
          userDao.findUserByUsernameOrEmail(userNameOrEmail, userNameOrEmail).flatMap {
            case Some(user) =>
              user.userData match {
                case Some(_) =>
                  // Generate a new Token to wait for the confirmation eMail
                  val token = UserToken(tokenType = UserToken.PASSWORD_CHANGE_SEPARATE_WINDOW_TOKEN)
                  // create a modifier document to change the last login date in the Database
                  val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                  // Push to the database using modifier
                  val modifier = BSONDocument(
                    "$set" ->
                    BSONDocument(User.DATEUPDATED -> bsonCurrentTime),
                    "$set" ->
                    BSONDocument(User.USERTOKEN -> token)
                  )
                  userSessionService.modifyUserWithCache(user.userID, modifier).map {
                    case Some(registeredUser) =>
                      // All done. User is registered, now send the welcome eMail
                      val eMail =
                        ResetPasswordMail(registeredUser, token.token, environment, env: Env)
                      eMail.send
                      Ok(passwordRequestSent)
                    case None =>
                      Ok(formError())
                  }

                case None =>
                  // User is not registered? Should not happen.
                  Future.successful(Ok(noSuchUser))
              }
            case None =>
              // No user found.
              Future.successful(Ok(noSuchUser))
          }
      }
    )
  }

  def resetPasswordChange: Action[AnyContent] = userAction.async { implicit request =>
    // Validate the password and return the new password Hash
    FormDefinitions.forgottenPasswordChange.bindFromRequest.fold(
      errors => Future.successful(Ok(formError(errors.errors.mkString(",\n")))), { newPasswordHash =>
        request.user.userToken match {
          case Some(token) =>
            if (token.tokenType == UserToken.PASSWORD_CHANGE_VERIFIED_TOKEN && token.userID.isDefined) {
              val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
              // Push to the database using modifier
              val modifier =
                BSONDocument(
                  "$set" ->
                  BSONDocument(User.DATEUPDATED -> bsonCurrentTime, User.PASSWORD -> newPasswordHash),
                  "$unset" ->
                  BSONDocument(User.USERTOKEN -> "")
                )
              userSessionService.modifyUserWithCache(token.userID.get, modifier).flatMap {
                case Some(userWithUpdatedAccount) =>
                  userSessionService
                    .modifyUserWithCache(
                      userWithUpdatedAccount.userID,
                      BSONDocument(
                        "$unset" ->
                        BSONDocument(User.USERTOKEN -> "")
                      )
                    )
                    .map {
                      case Some(updatedUser) =>
                        // All done. Now send the eMail to notify the user that the password has been changed
                        val eMail = PasswordChangedMail(updatedUser, environment, env: Env)
                        eMail.send
                        Ok(passwordChanged(updatedUser))
                      case None =>
                        Ok(databaseError)
                    }
                case None =>
                  // User has been found in the DB at first but now it cant be retrieved
                  Future.successful(Ok(databaseError))
              }
            } else {
              Future.successful(Ok(tokenMismatch()))
            }
          case None => Future.successful(Ok(tokenNotFound()))
        }
      }
    )
  }

  def passwordChangeSubmit(): Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    // remove user from cache to prevent invalid states
    userSessionService.removeUserFromCache(user)
    user.userData match {
      case Some(_) =>
        // Validate the password and return the new password Hash
        FormDefinitions
          .profilePasswordEdit(user)
          .bindFromRequest
          .fold(
            errors =>
              Future.successful {
                Ok(formError(errors.errors.mkString(",\n")))
              },
            // when there are no errors, then insert the user to the collection
            {
              case Some(newPasswordHash) =>
                val newSessionId: BSONObjectID = BSONObjectID.generate()
                userDao.changePassword(user.userID, newPasswordHash, newSessionId).map {
                  case Some(updatedUser) =>
                    userSessionService.updateUserInCache(updatedUser)
                    // Logout all other clients by renewing session id only for request
                    Ok(passwordChanged(updatedUser)).withSession(
                      userSessionService.sessionCookie(request, newSessionId)
                    )
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Ok(loginError())
                }
              case None =>
                // Password was incorrect
                fuccess(Ok(passwordWrong()))
            }
          )
      case None =>
        // User was not logged in
        fuccess(Ok(notLoggedIn()))
    }
  }

  def profileSubmit(): Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    user.userData match {
      case Some(userData) =>
        // change the userData with the help of the form input
        FormDefinitions
          .profileEdit(user)
          .bindFromRequest
          .fold(
            _ => fuccess(Ok(formError())),
            // when there are no errors, then insert the user to the collection
            {
              case Some(editedProfileUserData) =>
                // check that new email does not exist already
                if (editedProfileUserData.eMail != user.getUserData.eMail) {
                  userDao.findUserByEmail(editedProfileUserData.eMail).flatMap {
                    case Some(_) =>
                      fuccess(Ok(accountEmailUsed()))
                    case None => fuccess(NotFound)
                  }
                }

                userDao.updateUserData(user.userID, editedProfileUserData.copy(nameLogin = userData.nameLogin)).map {
                  case Some(updatedUser) =>
                    // Everything is ok, let the user know that they are logged in now
                    userSessionService.updateUserInCache(updatedUser)
                    Ok(editSuccessful(updatedUser))
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Ok(loginError())
                }

              case None =>
                // Password was incorrect
                fuccess(Ok(passwordWrong()))
            }
          )
      case None =>
        // User was not logged in
        fuccess(Ok(notLoggedIn()))
    }
  }

}
