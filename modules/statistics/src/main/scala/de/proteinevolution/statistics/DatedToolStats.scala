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

package de.proteinevolution.statistics

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class WeeklyToolStats(
    year: Int,
    week: Int,
    toolStats: ToolStatisticCollection
)

object WeeklyToolStats {
  implicit val toolStatsEncoder: Encoder[WeeklyToolStats] = deriveEncoder[WeeklyToolStats]
}

case class MonthlyToolStats(
    year: Int,
    month: Int,
    toolStats: ToolStatisticCollection
)

object MonthlyToolStats {
  implicit val toolStatsEncoder: Encoder[MonthlyToolStats] = deriveEncoder[MonthlyToolStats]
}