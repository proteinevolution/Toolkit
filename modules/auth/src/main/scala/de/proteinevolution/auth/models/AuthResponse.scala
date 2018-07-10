package de.proteinevolution.auth.models

trait AuthResponse {

  def msg: String

}

object AuthResponse {

  case object LoginIncorrect extends AuthResponse {
    def msg = "There was an error logging you in. Please check your account name and password."
  }

}
