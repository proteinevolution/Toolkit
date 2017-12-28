package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.database.statistics.{ JobEvent, JobEventLog }
import models.tools.ToolFactory
import de.proteinevolution.db.MongoStore
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class Tool @Inject()(mongoStore: MongoStore, toolFactory: ToolFactory, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends AbstractController(cc)
    with I18nSupport {

  /**
   * counts usage of frontend tools in order to keep track for our stats
   * @param toolName name of the Frontend Tool
   * @return
   */
  def frontendCount(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    if (toolFactory.isTool(toolName)) {
      // Add Frontend Job to Database
      mongoStore
        .addJobLog(
          JobEventLog(toolName = toolName.trim.toLowerCase, events = List(JobEvent(Done, Some(ZonedDateTime.now))))
        )
        .map { _ =>
          Ok
        }
    } else {
      Future.successful(BadRequest)
    }
  }
}
