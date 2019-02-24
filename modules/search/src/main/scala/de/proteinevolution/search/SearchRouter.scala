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

package de.proteinevolution.search

import de.proteinevolution.search.controllers.SearchController
import javax.inject.{ Inject, Singleton }
import play.api.routing.SimpleRouter
import play.api.routing.Router.Routes
import play.api.routing.sird._

@Singleton
class SearchRouter @Inject()(searchController: SearchController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/index/page/info")  => searchController.recentJobInfo
    case GET(p"/check/tool/$tool") => searchController.existsTool(tool)
    case GET(p"/tool/list")        => searchController.getToolList
    case GET(p"/suggest/$jobID")   => searchController.autoComplete(jobID)
  }

}
