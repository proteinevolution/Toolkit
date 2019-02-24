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

package de.proteinevolution.cluster.api

import scala.xml.{ Elem, XML }
import java.time.{ ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter

import de.proteinevolution.cluster.api.QStat.QStatJob

import scala.xml._

object QStat {
  // The QStat dates are formatted in the Default ISO format but without a Zone
  val qstatDateTimePattern: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault())

  // Predefined XML Tags for parsing
  val JOBID          = "JB_job_number"
  val STATE          = "state"
  val SUBMISSIONDATE = "JB_submission_time"
  val STARTDATE      = "JAT_start_time"
  val EXECUTIONDATE  = "JB_execution_time"

  // QStat Job objects contain the sge job ID, the state of the job and the time of the last event
  case class QStatJob(sgeID: String, state: String, date: ZonedDateTime) {
    val isQueued: Boolean     = state.trim == "qw"
    val isStarted: Boolean    = state.trim == "r"
    val hasFailed: Boolean    = state.trim == "E"
    val badRunscript: Boolean = state.trim == "Eqw"
  }

  // Parser for QStatJobs
  object QStatJob {
    def parse(node: Node): QStatJob = {
      val date: String = {
        if (node.contains(QStat.STARTDATE)) (node \ QStat.STARTDATE).text
        else if (node.contains(QStat.SUBMISSIONDATE)) (node \ QStat.SUBMISSIONDATE).text
        else if (node.contains(QStat.EXECUTIONDATE)) (node \ QStat.EXECUTIONDATE).text
        else "2017-01-01T00:00:00"
      }
      QStatJob(
        (node \ QStat.JOBID).text,
        (node \ QStat.STATE).text,
        ZonedDateTime.parse(date, QStat.qstatDateTimePattern)
      )
    }
  }
}

case class QStat(private val xml: String) {
  // Parse the xml first
  private val parsed: Elem = XML.loadString(xml)

  // Return the QStatJobs second
  val qStatJobs: List[QStatJob] = {
    (parsed \ "_" \ "job_list").map(n => QStatJob.parse(n)).toList
  }

  def totalJobs(status: String = ""): Int = status match {
    case "running" => qStatJobs.count(_.state.trim == "r")
    case "queued"  => qStatJobs.count(_.state.trim == "qw")
    case _         => qStatJobs.length
  }

}
