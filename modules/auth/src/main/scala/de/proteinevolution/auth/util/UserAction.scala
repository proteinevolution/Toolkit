package de.proteinevolution.auth.util
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.base.helpers.ToolkitTypes
import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UserAction @Inject()(val parser: BodyParsers.Default, userSessionService: UserSessionService)(
    implicit val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest]
    with ToolkitTypes {
  def transform[A](request: Request[A]): Future[UserRequest[A]] = {
    userSessionService.getUserFromCache(request).flatMap(user => fuccess(new UserRequest(user, request)))
  }
}
