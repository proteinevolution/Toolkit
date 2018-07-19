package de.proteinevolution.auth

import de.proteinevolution.auth.controllers.{
  AuthController,
  ValidationController
}
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class AuthRouter @Inject()(
    authController: AuthController,
    validationController: ValidationController
) extends SimpleRouter {

  private lazy val authRoutes: Routes = {
    case GET(p"/signout")                => authController.signOut
    case GET(p"/user/data")              => authController.getUserData
    case POST(p"/signup")                => authController.signUpSubmit
    case POST(p"/reset/password")        => authController.resetPassword
    case POST(p"/reset/password/change") => authController.resetPasswordChange
    case POST(p"/password")              => authController.passwordChangeSubmit()
    case POST(p"/profile")               => authController.profileSubmit()
    case POST(p"/signin")                => authController.signInSubmit
  }

  private lazy val validationRoutes: Routes = {
    case POST(p"/validate/modeller" ? q_o"input=$input") =>
      validationController.validateModellerKey(input)
  }

  override def routes: Routes = {
    authRoutes.orElse(validationRoutes)
  }

}
