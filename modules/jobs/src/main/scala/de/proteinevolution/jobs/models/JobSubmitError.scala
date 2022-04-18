/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
