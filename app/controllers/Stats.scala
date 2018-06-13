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
final class Stats @Inject()(mongoStore: MongoStore, toolFactory: ToolFactory, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends AbstractController(cc)
    with I18nSupport {

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
