package de.proteinevolution.jobs.models

trait JobSubmitError {
  def msg: String
}

object JobSubmitError {

  case object Undefined extends JobSubmitError {
    override val msg = "job could not be submitted"
  }

  case object FormError extends JobSubmitError {
    override val msg = "invalid form or parneters"
  }

  case object InvalidJobID extends JobSubmitError {
    override val msg = "job id is invalid"
  }

  case object DataBaseError extends JobSubmitError {
    override val msg = "could not write into database"
  }

  case object AlreadyTaken extends JobSubmitError {
    override val msg = "job id is already taken"
  }

  case object ModellerKeyInvalid extends JobSubmitError {
    override val msg = "modeller key is missing or incorrect"
  }

}
