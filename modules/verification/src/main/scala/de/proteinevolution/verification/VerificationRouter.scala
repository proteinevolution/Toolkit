package de.proteinevolution.verification

import de.proteinevolution.verification.controllers.VerificationController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class VerificationRouter @Inject()(ctrl: VerificationController)
    extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/$userName/$token") => ctrl.verification(userName, token)
  }

}
