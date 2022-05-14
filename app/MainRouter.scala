/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import controllers.UptimeController
import de.proteinevolution.auth.AuthRouter
import de.proteinevolution.backend.BackendRouter
import de.proteinevolution.jobs.JobsRouter
import de.proteinevolution.message.MessageRouter
import de.proteinevolution.ui.UiRouter
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class MainRouter @Inject(
    uptime: UptimeController,
    authRouter: AuthRouter,
    backendRouter: BackendRouter,
    jobsRouter: JobsRouter,
    uiRouter: UiRouter,
    messageRouter: MessageRouter
) extends SimpleRouter {

  private lazy val mainRoutes: Routes = {
    case GET(p"/uptime")    => uptime.uptime
    case GET(p"/buildinfo") => uptime.buildInfo
  }

  override lazy val routes: Routes = {
    mainRoutes
      .orElse(messageRouter.withPrefix("/ws").routes)
      .orElse(uiRouter.withPrefix("/api/tools").routes)
      .orElse(jobsRouter.withPrefix("/api/jobs").routes)
      .orElse(backendRouter.withPrefix("/api/backend").routes)
      .orElse(authRouter.withPrefix("/api/auth").routes)
  }

}
