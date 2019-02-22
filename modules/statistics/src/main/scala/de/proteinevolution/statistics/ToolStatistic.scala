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

package de.proteinevolution.statistics

import io.circe.Encoder
import io.circe.generic.semiauto._
import reactivemongo.bson._

case class ToolStatistic(
    toolName: String,
    monthly: List[Int] = List.empty[Int],
    monthlyFailed: List[Int] = List.empty[Int],
    monthlyDeleted: List[Int] = List.empty[Int],
    monthlyInternal: List[Int] = List.empty[Int]
) {

  /**
   * Returns the total amount of jobs used with the tool
   */
  def total: Long = monthly.map(_.toLong).sum[Long]

  /**
   * Returns the total amount of failed jobs used with the tool
   */
  def totalFailed: Long = monthlyFailed.map(_.toLong).sum[Long]

  /**
   * Returns the total amount of deleted jobs used with the tool
   */
  def totalDeleted: Long = monthlyFailed.map(_.toLong).sum[Long]

  /**
   * Returns the total amount of internal jobs used with the tool
   */
  def totalInternal: Long = monthlyInternal.map(_.toLong).sum[Long]

  /**
   * Creates a new ToolStatistic object which is a copy of this one - with the current month added
   */
  def addMonth(current: Int, currentFailed: Int, currentDeleted: Int, currentInternal: Int): ToolStatistic = {
    this.copy(
      monthly = monthly.::(current),
      monthlyFailed = monthlyFailed.::(currentFailed),
      monthlyDeleted = monthlyDeleted.::(currentDeleted),
      monthlyInternal = monthlyInternal.::(currentInternal)
    )
  }

  /**
   * Creates a new ToolStatistic object which is a copy of this one - with the current month added
   */
  def addMonths(
      currents: List[Int],
      currentsFailed: List[Int],
      currentsDeleted: List[Int],
      currentsInternal: List[Int]
  ): ToolStatistic = {
    this.copy(
      monthly = monthly ::: currents,
      monthlyFailed = monthlyFailed ::: currentsFailed,
      monthlyDeleted = monthlyDeleted ::: currentsDeleted,
      monthlyInternal = monthlyInternal ::: currentsInternal
    )
  }

  /**
   * Adds empty months "amount" of times recursively
   */
  def addEmptyMonths(amount: Int = 1): ToolStatistic = {
    if (amount > 0) this.addMonth(0, 0, 0, 0).addEmptyMonths(amount - 1)
    else this
  }
}

object ToolStatistic {

  val TOOLNAME        = "tool"
  val MONTHLY         = "monthly"
  val MONTHLYFAILED   = "monthlyFailed"
  val MONTHLYDELETED  = "monthlyDeleted"
  val MONTHLYINTERNAL = "monthlyInternal"

  implicit val toolStatsEncoder: Encoder[ToolStatistic] = deriveEncoder[ToolStatistic]

  implicit object Reader extends BSONDocumentReader[ToolStatistic] {
    def read(bson: BSONDocument): ToolStatistic = {
      ToolStatistic(
        toolName = bson.getAs[String](TOOLNAME).getOrElse("invalid"),
        monthly = bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty),
        monthlyFailed = bson.getAs[List[Int]](MONTHLYFAILED).getOrElse(List.empty),
        monthlyDeleted = bson.getAs[List[Int]](MONTHLYDELETED).getOrElse(List.empty),
        monthlyInternal = bson.getAs[List[Int]](MONTHLYINTERNAL).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ToolStatistic] {
    def write(toolStatistic: ToolStatistic): BSONDocument = BSONDocument(
      TOOLNAME        -> toolStatistic.toolName,
      MONTHLY         -> toolStatistic.monthly,
      MONTHLYFAILED   -> toolStatistic.monthlyFailed,
      MONTHLYDELETED  -> toolStatistic.monthlyDeleted,
      MONTHLYINTERNAL -> toolStatistic.monthlyDeleted
    )
  }

}
