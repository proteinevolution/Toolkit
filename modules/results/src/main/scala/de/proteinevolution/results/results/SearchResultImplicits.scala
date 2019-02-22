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

package de.proteinevolution.results.results

import io.circe._
import io.circe.syntax._

// https://github.com/circe/circe/issues/216

trait SearchResultImplicits {

  implicit val encodeDoubleOrIntOrString: Encoder[Either[Either[Double, Int], String]] =
    Encoder.instance(_.fold(_.fold(_.asJson, _.asJson), _.asJson))

  implicit val encodeDoubleOrString: Encoder[Either[Double, String]] =
    Encoder.instance(_.fold(_.asJson, _.asJson))
}

object SearchResultImplicits extends SearchResultImplicits
