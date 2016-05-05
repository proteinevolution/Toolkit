package models.auth

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by astephens on 03.04.16.
  */

/**
  * Form mapping for the Sign in form
  */
object SignIn {
  val inputForm = Form(
    tuple(
      "user_login" -> nonEmptyText,
      "password"   -> nonEmptyText
    )
  )
}

/**
  * Form mapping for the Sign up form
  */
object SignUp {
  val inputForm = Form(
    tuple(
      "name_login"    -> nonEmptyText,
      "name_first"    -> nonEmptyText,
      "name_last"     -> nonEmptyText,
      "email"         -> email,
      "password"      -> nonEmptyText,
      "accepttos"     -> boolean,
      "passwordmatch" -> boolean
    )
  )
}
