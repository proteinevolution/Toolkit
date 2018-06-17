package de.proteinevolution.jobs.controllers

import javax.inject.Inject
import play.api.mvc.{ AbstractController, ControllerComponents }

class SubmissionController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def test() = Action {
    Ok
  }

}
