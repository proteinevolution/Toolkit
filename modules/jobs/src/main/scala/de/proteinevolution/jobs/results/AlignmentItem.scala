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

package de.proteinevolution.jobs.results

import io.circe.{ DecodingFailure, Json }

case class AlignmentItem(accession: String, seq: String, num: Int)

object AlignmentItem {

  implicit def alignmentItemDecoder(j: Json, i: Int): Either[DecodingFailure, AlignmentItem] =
    for {
      accession <- j.hcursor.downArray.first.as[String]
      seq       <- j.hcursor.downArray.right.as[String]
    } yield new AlignmentItem(accession, seq, i + 1)

}
