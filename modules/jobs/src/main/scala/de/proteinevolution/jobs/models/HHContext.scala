package de.proteinevolution.jobs.models

import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.db.ResultFileAccessor
import javax.inject.Inject
import play.api.mvc.ControllerComponents

case class HHContext @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    controllerComponents: ControllerComponents
)
