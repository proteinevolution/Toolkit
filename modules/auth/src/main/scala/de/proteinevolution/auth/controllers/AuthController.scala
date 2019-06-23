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

import java.util.UUID

import akka.actor.ActorRef
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate._
import de.proteinevolution.auth.models.Session.{ChangeSessionID, LogOut}
import de.proteinevolution.auth.models.{FormDefinitions, JSONTemplate}
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.user.{AccountType, UserToken}
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.cache.{NamedCache, SyncCacheApi}
import play.api.libs.mailer.MailerClient
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.api.{Configuration, Logging}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
    userSessionService: UserSessionService,
    userDao: UserDao,
    cc: ControllerComponents,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    config: Configuration,
    userAction: UserAction
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends ToolkitController(cc)
    with JSONTemplate
    with Logging {

  def signOut: Action[AnyContent] = userAction { implicit request =>
    wsActorCache.get[List[ActorRef]](request.user.userID) match {
      case Some(wsActors) =>
        val actorList: List[ActorRef] = wsActors: List[ActorRef]
        actorList.foreach(_ ! LogOut())
        wsActorCache.remove(request.user.userID)
      case None =>
    }
    userSessionService.removeUserFromCache(request.user)
    Ok(loggedOut()).withNewSession
  }

  def getUserData: Action[AnyContent] = userAction { implicit request =>
    logger.info("Sending user data.")
    Ok(request.user.userData.asJson).withSession(
      // this is very important as it is a call every frontend makes. Perfect place to enforce a session
      userSessionService.sessionCookie(request, request.user.sessionID.getOrElse(""))
    )
  }

  def signInSubmit: Action[AnyContent] = userAction.async { implicit request =>
    val anonymousUser = request.user
    // check if unregistered user (accountType == -1)
    if (anonymousUser.accountType < 0) {
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

                userDao.saveNewLogin(databaseUser).map {
                  case Some(loggedInUser) =>
                    logger.info(
                      "-[new login]-\n"
                      + loggedInUser.toString
                    )

                    // Tell other tabs that login happened
                    wsActorCache.get[List[ActorRef]](anonymousUser.userID) match {
                      case Some(wsActors) =>
                        val actorList: List[ActorRef] = wsActors: List[ActorRef]
                        wsActorCache.set(loggedInUser.userID, actorList)
                        actorList.foreach(_ ! ChangeSessionID(loggedInUser.sessionID.get))
                        wsActorCache.remove(anonymousUser.userID)
                      case None =>
                    }

                    // Remove the old, not logged in user
                    userDao.removeUsers(List(anonymousUser.userID))
                    userSessionService.removeUserFromCache(anonymousUser)
                    userSessionService.updateUserInCache(loggedInUser)

                    // add the anonymous jobs to the user // TODO update jobs to new owner id
                    userDao.addJobsToUser(loggedInUser.userID, anonymousUser.jobs)

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
    if (user.accountType < AccountType.NORMALUSERAWAITINGREGISTRATION) {
      // Create a new user from the information given in the form
      FormDefinitions
        .signUp(user)
        .bindFromRequest
        .fold(
          errors =>
            Future.successful {
              Ok(formError(errors.errors.mkString(",\n")))
            },
          // if no error, then insert the user to the collection
          signUpFormUser => {
            if (signUpFormUser.accountType < AccountType.NORMALUSERAWAITINGREGISTRATION) {
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
                      userID = UUID.randomUUID().toString,
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
                            NewUserWelcomeMail(registeredUser, token.token, config).send
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

  def forgotPassword: Action[AnyContent] = Action.async { implicit request =>
    FormDefinitions.forgottenPasswordRequest.bindFromRequest.fold(
      errors =>
        Future.successful {
          Ok(formError(errors.errors.mkString(",\n")))
        },
      // when there are no errors, then insert the user to the collection
      {
        case Some(userNameOrEmail: String) =>
          userDao.findUserByUsernameOrEmail(userNameOrEmail, userNameOrEmail).flatMap {
            case Some(user) =>
              user.userData match {
                case Some(_) =>
                  // Generate a new Token to wait for the confirmation eMail
                  val token = UserToken(tokenType = UserToken.PASSWORD_CHANGE_TOKEN)
                  userDao.setToken(user.userID, token).map {
                    case Some(registeredUser) =>
                      userSessionService.updateUserInCache(registeredUser)
                      // All done. User is registered, now send the welcome eMail
                      ResetPasswordMail(registeredUser, token.token, config).send
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

  def changeForgottenPasswordSubmit: Action[AnyContent] = userAction.async { implicit request =>
    // Validate the password and return the new password Hash
    FormDefinitions.forgottenPasswordChange.bindFromRequest.fold(
      errors => Future.successful(Ok(formError(errors.errors.mkString(",\n")))), { formContent =>
        userDao.findUserByUsername(formContent._2).flatMap {
          case Some(userToVerify) =>
            userToVerify.userToken match {
              case Some(userToken) =>
                if (userToken.token == formContent._3 && userToken.tokenType == UserToken.PASSWORD_CHANGE_TOKEN) {
                  val newSessionId = UUID.randomUUID().toString
                  userDao.changePassword(userToVerify.userID, formContent._1, newSessionId).map {
                    case Some(updatedUser) =>
                      userSessionService.updateUserInCache(updatedUser)
                      // All done. Now send the eMail to notify the user that the password has been changed
                      PasswordChangedMail(updatedUser, config).send
                      // Logout all other clients by renewing session id only for request
                      Ok(passwordChanged(updatedUser)).withSession(
                        userSessionService.sessionCookie(request, newSessionId)
                      )
                    case None =>
                      // User has been found in the DB at first but now it cant be retrieved
                      Ok(databaseError)
                  }
                } else {
                  // No Token in DB
                  Future.successful(Ok(tokenMismatch()))
                }
              case None => Future.successful(Ok(tokenNotFound()))
            }
          case None => Future.successful(Ok(accountError()))
        }
      }
    )
  }

  def changePasswordSubmit: Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
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
                val newSessionId = UUID.randomUUID().toString
                userDao.changePassword(user.userID, newPasswordHash, newSessionId).map {
                  case Some(updatedUser) =>
                    userSessionService.updateUserInCache(updatedUser)
                    // All done. Now send the eMail to notify the user that the password has been changed
                    PasswordChangedMail(updatedUser, config).send
                    // Logout all other clients by renewing session id only for request
                    Ok(passwordChanged(updatedUser)).withSession(
                      userSessionService.sessionCookie(request, newSessionId)
                    )
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Ok(loginError()).withSession(
                      userSessionService.sessionCookie(request, newSessionId)
                    )
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

  def profileSubmit: Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    user.userData match {
      case Some(userData) =>
        // change the userData with the help of the form input
        FormDefinitions
          .profileEdit(user)
          .bindFromRequest
          .fold(
            errors =>
              Future.successful {
                Ok(formError(errors.errors.mkString(",\n")))
              },
            // when there are no errors, then insert the user to the collection
            {
              case Some(editedProfileUserData) =>
                // check that new email does not exist already
                if (editedProfileUserData.eMail != user.getUserData.eMail) {
                  userDao.findUserByEmail(editedProfileUserData.eMail).map {
                    case Some(_) =>
                      Ok(accountEmailUsed())
                    case None => NotFound
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
                Future.successful(Ok(passwordWrong()))
            }
          )
      case None =>
        // User was not logged in
        Future.successful(Ok(notLoggedIn()))
    }
  }

}
