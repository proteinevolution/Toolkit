package modules.parsers.Ops

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import modules.parsers.Ops.QStat.QStatJob

import scala.xml._

object QStat {
  val qstatDateTimePattern : DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault())

  val JOBID = "JB_job_number"
  val STATE = "state"
  val SUBMISSIONDATE = "JB_submission_time"
  val STARTDATE = "JAT_start_time"
  val EXECUTIONDATE = "JB_execution_time"

  case class QStatJob(sgeID : String, state : String, date : ZonedDateTime) {
    val isStarted : Boolean = state.contains("r")
  }

  object QStatJob {
    def parse(node : Node) : QStatJob = {
      val date : String = {
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
case class QStat(private val xml : String) {
  private val parsed : Elem = XML.loadString(xml)
  val qStatJobs : List[QStatJob] = {
    (parsed \ "_" \ "job_list").map(n => QStatJob.parse(n)).toList
  }

  def totalJobs : Int = qStatJobs.length

  def runningJobs : Int = qStatJobs.count(_.state.contains("r"))

  def queuedJobs : Int = qStatJobs.count(_.state.contains("q"))
}
