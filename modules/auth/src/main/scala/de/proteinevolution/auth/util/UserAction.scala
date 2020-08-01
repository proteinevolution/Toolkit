/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.auth.util
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.base.helpers.ToolkitTypes
import javax.inject.{Inject, Singleton}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAction @Inject()(val parser: BodyParsers.Default, userSessions: UserSessionService)(
    implicit val executionContext: ExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest]
    with ToolkitTypes {
  def transform[A](request: Request[A]): Future[UserRequest[A]] = {
    userSessions.getUser(request).flatMap(user => fuccess(new UserRequest(user, request)))
  }
}
