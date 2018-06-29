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
