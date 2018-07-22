package de.proteinevolution.jobs.services

import java.time.{ Duration, ZonedDateTime }

import de.proteinevolution.cluster.api.QStat
import de.proteinevolution.models.database.jobs.{ Job, JobState }

trait JobTerminator {

  // checks whether the jobs has exceeded the hardruntime and should be killed by the SGE by now
  def isOverDue(job: Job): Boolean = {
    val jobIsDead = job.clusterData.map { cd =>
      val startDate = cd.dateStarted.getOrElse(ZonedDateTime.now())
      val runtime   = cd.hardruntime.getOrElse(0).toLong
      val isRunning = job.status == JobState.Running && cd.dateFinished.isEmpty
      val duration  = Duration.between(startDate, ZonedDateTime.now()).getSeconds
      duration > runtime && isRunning
    }
    jobIsDead.getOrElse(false)
  }

  def sgeFailed(sgeId: String, qStat: QStat): Boolean = {
    qStat.qStatJobs.find(sgeJob => sgeJob.sgeID == sgeId).exists(qStat => qStat.hasFailed || qStat.badRunscript)
  }

}
