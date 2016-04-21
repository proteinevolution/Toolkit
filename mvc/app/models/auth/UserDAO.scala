package models.auth

import javax.inject.{Singleton, Inject}

import models.database.{User, Users}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future

/**
  * Created by astephens on 21.04.16.
  */
@Singleton
class UserDAO @Inject () (userDB : Users) {

}
