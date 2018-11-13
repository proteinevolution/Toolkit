package de.proteinevolution.search.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.search.services.SearchService
import de.proteinevolution.tools.ToolConfig
import io.circe.JsonObject
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class SearchController @Inject()(
    cc: ControllerComponents,
    userSessions: UserSessions,
    toolConfig: ToolConfig,
    searchService: SearchService
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc) {

  /**
   * Returns a json object containing both, the last updated job and the most recent total number of jobs.
   */
  def recentJobInfo: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      searchService.recentJob(user).map { lastJob =>
        Ok(JsonObject("lastJob" -> lastJob.map(_.cleaned(toolConfig)).asJson).asJson)
      }
    }
  }

  def existsTool(queryString: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { _ =>
      if (toolConfig.isTool(queryString)) {
        Ok(true.asJson)
      } else {
        NotFound
      }
    }
  }

  def getToolList: Action[AnyContent] = Action {
    Ok(
      toolConfig.values.values
        .filterNot(_.toolNameShort == "hhpred_manual")
        .map(a => JsonObject("long" -> a.toolNameLong.asJson, "short" -> a.toolNameShort.asJson))
        .asJson
    )
  }

  /**
   * if no tool is found for a given query,
   * it looks for jobs which belong to the current user.
   * only jobIDs that belong to the user are autocompleted
   */
  def autoComplete(queryString_ : String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      searchService.autoComplete(user, queryString_).value.map {
        case Some(jobs) => Ok(jobs.map(_.cleaned(toolConfig)).asJson)
        case None       => NoContent
      }
    }
  }

}
