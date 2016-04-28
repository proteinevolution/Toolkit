package models.auth

import javax.inject.{Singleton, Inject}

import models.database.User


/**
  * Created by astephens on 21.04.16.
  */
@Singleton
class UserDAO @Inject () (userDB : User) {

}
