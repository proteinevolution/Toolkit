package controllers.headless

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.services.JobIdProvider
import play.api.mvc.{ AbstractController, ControllerComponents }

@Singleton
class APIController @Inject()(cc: ControllerComponents, jobIdProvider: JobIdProvider) extends AbstractController(cc) {

  def submit() = Action { _ =>

    Ok(jobIdProvider.provide)

  }

}
