/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
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

package de.proteinevolution.common.models.database.jobs

import de.proteinevolution.base.helpers.ToolkitTypes._
import io.circe.{ Decoder, Encoder, HCursor }
import reactivemongo.api.bson.{ BSONInteger, BSONReader, BSONValue, BSONWriter }
import shapeless._

import scala.collection.immutable
import scala.util.{ Success, Try }

sealed trait JobState {
  def toInt: Int
}

object JobState {

  case object Prepared extends JobState {
    override def toInt = 1
  }

  case object Queued extends JobState {
    override def toInt = 2
  }

  case object Running extends JobState {
    override def toInt = 3
  }

  case object Error extends JobState {
    override def toInt = 4
  }

  case object Done extends JobState {
    override def toInt = 5
  }

  case object Submitted extends JobState {
    override def toInt = 6
  }

  case object Pending extends JobState {
    override def toInt = 7
  }

  case object LimitReached extends JobState {
    override def toInt = 8
  }

  case object Deleted extends JobState {
    override def toInt = 9
  }

  private def states: immutable.Seq[JobState] =
    implicitly[AllSingletons[
      JobState,
      Prepared.type :+:
      Queued.type :+:
      Running.type :+:
      Done.type :+:
      Error.type :+:
      Submitted.type :+:
      Pending.type :+:
      LimitReached.type :+:
      CNil
    ]].values

  implicit val jobStateDecoder: Decoder[JobState] = (c: HCursor) =>
    c.downField("status").as[Int].map(n => states.find(_.toInt == n).getOrElse(Error))

  implicit val jobStateEncoder: Encoder[JobState] = Encoder[Int].contramap(_.toInt)

  implicit object JobStateReader extends BSONReader[JobState] {
    def readTry(state: BSONValue): Try[JobState] =
      for {
        i <- state.asTry[BSONInteger]
      } yield states.find(_.toInt == i.value).getOrElse(Error)
  }

  implicit object JobStateWriter extends BSONWriter[JobState] {
    def writeTry(state: JobState): Try[BSONValue] = Success(BSONInteger(state.toInt))
  }

}
