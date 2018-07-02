package de.proteinevolution.message

import de.proteinevolution.message.controllers.MessageController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class MessageRouter @Inject()(messageController: MessageController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/") => messageController.ws
  }

}
