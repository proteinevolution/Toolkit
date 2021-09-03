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

package de.proteinevolution.jobs.services

import cats.effect.{ IO, Ref, Resource }
import de.proteinevolution.jobs.dao.JobDao
import javax.inject.{ Inject, Singleton }

import scala.collection.immutable.HashSet
import scala.concurrent.ExecutionContext
import scala.util.Random

@Singleton
final class JobIdProvider @Inject() (jobDao: JobDao)(implicit ec: ExecutionContext) {

  private[this] val generateSafe: HashSet[String] => IO[String] = { x =>
    IO(Iterator.continually[String](Random.nextInt(9999999).toString.padTo(7, '0')).filterNot(x.contains).next())
  }

  private[this] val generateUnsafe: IO[String] = IO(
    Iterator.continually[String](Random.nextInt(9999999).toString.padTo(7, '0')).next()
  )

  private[this] def validate(id: String): IO[Boolean] = {
    IO.fromFuture(IO.pure(jobDao.findJob(id).map(_.isEmpty)))
  }

  // recursively generate job ids which are not in the database
  def runUnsafe: IO[String] = {
    (for {
      id <- generateUnsafe
      b  <- validate(id)
    } yield (id, b)).flatMap { case (id, b) => if (b) IO.pure(id) else runUnsafe }
  }

  /**
   * recursively tries to generate a job id which is not already in the database while trying, don't use the same id
   * again for the next cycle
   */
  def runSafe: IO[String] = {
    Ref.of[IO, HashSet[String]](HashSet.empty[String]).flatMap { ref =>
      (for {
        jobId <- Resource.eval(ref.get).use(generateSafe)
        set   <- ref.get
      } yield (jobId, set)).flatMap { case (jobId, set) =>
        (for {
          b <- validate(jobId)
          _ <- ref.set(set.+(jobId))
        } yield b).flatMap { b =>
          if (b) {
            IO.pure(jobId)
          } else {
            runSafe
          }
        }
      }
    }
  }

}
