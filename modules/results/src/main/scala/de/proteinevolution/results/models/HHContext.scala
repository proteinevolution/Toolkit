package de.proteinevolution.results.models

import javax.inject.Inject

import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.models.ConstantsV2
import play.api.mvc.ControllerComponents

case class HHContext @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    controllerComponents: ControllerComponents
)
