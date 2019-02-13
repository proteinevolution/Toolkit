package de.proteinevolution.jobs.services

import better.files._
import de.proteinevolution.common.models.ConstantsV2

trait JobFolderValidation {

  protected def jobFolderIsValid(jobId: String, constants: ConstantsV2): Boolean = {
    (constants.jobPath / jobId).exists
  }

  protected def resultsExist(jobId: String, constants: ConstantsV2): Boolean = {
    (constants.jobPath / jobId).exists && (constants.jobPath / jobId / "results").exists
  }

  protected def paramsExist(jobId: String, constants: ConstantsV2): Boolean = {
    (constants.jobPath / jobId / "sparam").exists
  }

}
