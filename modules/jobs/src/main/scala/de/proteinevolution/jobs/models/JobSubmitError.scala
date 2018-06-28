package de.proteinevolution.jobs.models

trait JobSubmitError {
  def msg: String
}

object JobSubmitError {

  case object Undefined extends JobSubmitError {
    override val msg = "job could not be submitted"
  }

  case object InvalidJobID extends JobSubmitError {
    override val msg = "job id is invalid"
  }

}
