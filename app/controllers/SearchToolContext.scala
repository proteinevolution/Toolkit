package controllers

import javax.inject.Inject

import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.Constants
import org.webjars.play.WebJarsUtil
import play.api.mvc.ControllerComponents

case class SearchToolContext @Inject()(
    resultFiles: ResultFileAccessor,
    webJarsUtil: WebJarsUtil,
    constants: Constants,
    controllerComponents: ControllerComponents
)
