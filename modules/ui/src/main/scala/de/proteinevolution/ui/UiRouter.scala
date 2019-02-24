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

package de.proteinevolution.ui

import de.proteinevolution.ui.controllers.UiController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class UiRouter @Inject()(uiController: UiController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/tool/$toolName") => uiController.getTool(toolName)
    case GET(p"/static/$static") => uiController.static(static)
    case GET(p"/recent/updates") => uiController.recentUpdates
  }

}
