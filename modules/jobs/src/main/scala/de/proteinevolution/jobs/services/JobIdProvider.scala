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

import cats.effect.IO
import de.proteinevolution.jobs.dao.JobDao
import javax.inject.{ Inject, Singleton }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random

@Singleton
class JobIdProvider @Inject()(
    jobDao: JobDao,
    @volatile private var usedIds: ListBuffer[String] = new ListBuffer[String]()
)(implicit ec: ExecutionContext) {

  def provide: Future[String] = generate.unsafeToFuture()

  @inline def trash(id: String): Unit = {
    usedIds = usedIds - id
  }

  private def generate: IO[String] = {
    val id =
      Iterator.continually[String](Random.nextInt(9999999).toString.padTo(7, '0')).filterNot(usedIds.contains).next()
    validate(id).flatMap { b =>
      usedIds + id
      if (b) {
        IO.pure(id)
      } else {
        generate
      }
    }
  }

  private def validate(id: String): IO[Boolean] = {
    IO.fromFuture(IO.pure(jobDao.selectJob(id).map(_.isEmpty)))
  }

}
