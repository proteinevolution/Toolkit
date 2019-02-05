package de.proteinevolution.auth.util
import de.proteinevolution.models.database.users.User
import play.api.mvc.{ Request, WrappedRequest }

class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)
