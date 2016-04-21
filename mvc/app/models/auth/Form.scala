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
      "username" -> email,
      "password" -> nonEmptyText
    )
  )
}

/**
  * Form mapping for the Sign up form
  */
object SignUp {
  val inputForm = Form(
    tuple(
      "namefirst"     -> nonEmptyText,
      "namelast"      -> nonEmptyText,
      "email"         -> email,
      "password"      -> nonEmptyText,
      "acceptagb"     -> boolean,
      "passwordmatch" -> boolean
    )
  )
}
