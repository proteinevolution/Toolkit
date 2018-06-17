package de.proteinevolution.auth.models

sealed trait AuthError

object AuthError {

  case object AccountNameUsed extends AuthError
  case object FormError       extends AuthError
  case object MustAcceptToS   extends AuthError
  case object TokenMismatch   extends AuthError

}
