package de.proteinevolution.jobs.models

trait JobHashError {
  def msg: String
}

object JobHashError {

  case object JobNotFound extends JobHashError {
    val msg: String = "job not found in database"
  }

  case object JobIsPrivate extends JobHashError {
    val msg: String = "job not found"
  }

}
