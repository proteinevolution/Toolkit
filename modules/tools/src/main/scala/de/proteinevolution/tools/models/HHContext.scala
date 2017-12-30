package de.proteinevolution.tools.models

import javax.inject.Inject

import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.Constants
import org.webjars.play.WebJarAssets
import play.api.mvc.ControllerComponents

case class HHContext @Inject()(
    webJarAssets: WebJarAssets,
    resultFiles: ResultFileAccessor,
    constants: Constants,
    controllerComponents: ControllerComponents
)
