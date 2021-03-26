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

package de.proteinevolution.backend

import de.proteinevolution.backend.controllers.BackendController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class BackendRouter @Inject() (ctrl: BackendController) extends SimpleRouter {

  private lazy val backendRoutes: Routes = {
    case GET(p"/statistics")        => ctrl.statistics
    case GET(p"/runusersweep")      => ctrl.runUserSweep
    case GET(p"/runjobsweep")       => ctrl.runJobSweep
    case GET(p"/users")             => ctrl.users
    case GET(p"/maintenance")       => ctrl.getMaintenanceMode
    case POST(p"/users")            => ctrl.users
    case POST(p"/startmaintenance") => ctrl.sendMaintenanceAlert(true)
    case POST(p"/endmaintenance")   => ctrl.sendMaintenanceAlert(false)
  }

  override lazy val routes: Routes = {
    backendRoutes
  }

}
