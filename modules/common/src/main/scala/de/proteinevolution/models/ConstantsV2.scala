package de.proteinevolution.models

import java.io.File

import javax.inject.{ Inject, Singleton }
import play.api.Configuration

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.util.matching.Regex

@Singleton
class ConstantsV2 @Inject()(config: Configuration) {

  /** Number of jobActors */
  val nJobActors: Int = 100

  /** Key for the Modeller tool */
  val modellerKey: String = config.get[String]("modeller_key")

  // File Settings
  /** File Seperator */
  val SEPARATOR: String = File.separator

  /** Path to the jobs folder */
  val jobPath: String = s"${config.get[String]("job_path")}$SEPARATOR"

  /** results folder name */
  val resultFolderName: String = "results"

  /** name of the parameter file */
  val serializedParam: String = "sparam"

  val formMultiValueSeparator: String = " "
  val breakAfterClustal: Int          = 85 // clustal format breaks after n chars

  // Job limitation settings
  val maxJobNum: Int        = 100  // max number of jobs that can be submitted from one ip within maxJobsWithin
  val maxJobsWithin: Int    = 1    // time in minutes within the max number of jobs is applied
  val maxJobNumDay: Int     = 4000 // max number of jobs that can be submitted from one ip within maxJobsWithinDay
  val maxJobsWithinDay: Int = 1    // time in days within the max number of jobs is applied for a day

  // Job deletion settings
  /** Sweeps at this time after server start */
  val jobDeletionDelay: FiniteDuration = 10 minutes

  /** Sweeps in this interval */
  val jobDeletionInterval: FiniteDuration = 3 hours

  /** all jobs of registered users that are older than the given number are permanently deleted everywhere */
  val jobDeletionRegistered: Int = 90

  /** all jobs of non registered users that are older than the given number are permanently deleted everywhere */
  val jobDeletion: Int = 24

  /** time in days in which the job should not have been viewed */
  val jobDeletionLastViewed: Int = 7

  // User Deletion settings
  /** Sweeps at this time after server start */
  val userDeletionDelay: FiniteDuration = 70 minutes

  /** Sweeps in this interval */
  val userDeletionInterval: FiniteDuration = 3 hours

  /** Sending an eMail to the user this many days before the deletion */
  val userDeletionWarning: Int = 14 //days
  /** Deletes regular accounts after this time frame */
  val userDeleting: Int = 1 //months
  /** Deletes users awaiting registration after this time frame */
  val userDeletingRegisterEmail: Int = 3 //days
  /** Deletes registered accounts after this time frame */
  val userDeletingRegistered: Int = 24 //months

  // Polling and cluster load checking settings
  /** Interval of the qstat requests */
  val pollingInterval: FiniteDuration = 5 seconds

  /** Maximum amount of strikes a job may have before it is set to the Error state */
  val pollingMaximumStrikes: Int = 10 // strikes

  /** The marker for 100% load capacity */
  val loadPercentageMarker: Int = 32 // Jobs

  /** Amount of elements stored in a load log record */
  val loadRecordElements: Int = 20 // elements

  // jobID pattern settings
  /** allowed elements in the jobID */
  val jobIDCharacters: String = "[0-9a-zA-Z_]"

  /** versioning character */
  val jobIDVersioningCharacter: String = "_"

  /** The regular jobID pattern to match against */
  val jobIDNoVersionPattern: Regex = s"($jobIDCharacters{3,96})".r

  /** The additional pattern for versioning */
  val jobVersionPattern: Regex = s"(?:$jobIDVersioningCharacter([0-9]{1,3}))".r

  /** The combined pattern */
  val jobIDPattern: Regex = (jobIDNoVersionPattern.regex + jobVersionPattern.regex).r

  /** The combined pattern with the version as an option */
  val jobIDVersionOptionPattern: Regex = (jobIDNoVersionPattern.regex + jobVersionPattern.regex + "?").r

}
