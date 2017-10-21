package controllers.headless

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import de.proteinevolution.test.Test

@Singleton
class APIController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def submit() = Action { _ =>

    Ok(Test.test)}

}
