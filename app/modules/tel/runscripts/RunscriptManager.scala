package modules.tel.runscripts

import javax.inject.{Inject, Named, Singleton}

import better.files._
import play.api.Logger

//TODO
// * Support Constraints and Conditions
// * Runscript Manager should watch the runscript Path for changes

/**
  * Class watches the directory of runscripts and monitors changes. Reloads information about runscripts once
  * the file changes and keeps a map of all Runscripts for quick access.
  * Allows to provide a runscript to an interested instance, like a JobActor
  *
  * Created by lzimmermann on 10/19/16.
  */
@Singleton
class RunscriptManager @Inject()(@Named("runscriptPath") runscriptPath: String) {

  // Constants for the RunscriptManager
  final val SUFFIX = "SUFFIX"
  final val PREFIX = "PREFIX"

  Logger.info(s"RunscriptManager started, surveilling: $runscriptPath")

  // Maps each runscript name to the corresponding file
  private final val runscripts: Map[String, File] = runscriptPath.toFile.list
    .withFilter(_.extension.getOrElse("") == ".sh")
    .map { file =>
      file.nameWithoutExtension -> file
    }
    .toMap

  // All files that will be prepended to a requested runscript
  private final val prefix: Seq[File] = {
    val x = runscriptPath.toFile / PREFIX
    if (x.exists) {
      Logger.info("RunscriptManager: PREFIX will be automatically prepended to each runscript")
      Seq(x)
    } else {
      Seq.empty
    }
  }
  // All files that will be appended to a requested runscript
  private final val suffix: Seq[File] = {
    val x = runscriptPath.toFile / SUFFIX
    if (x.exists) {
      Logger.info("RunscriptManager: SUFFIX will be automatically prepended to each runscript")
      Seq(x)
    } else {
      Seq.empty
    }
  }

  def apply(runscriptName: String): Runscript = Runscript(prefix ++ Seq(runscripts(runscriptName)) ++ suffix)
}

// An argument is either valid or invalid. If it is invalid, it can be represented, otherwise it has
// a collection of violated Constraints
sealed trait Argument
case class InvalidArgument(violatedConstraints: Seq[RType => Boolean]) extends Argument
case class ValidArgument(representation: Representation)               extends Argument

sealed trait RType {

  def inner(): String
}
case class RString(x: String) extends RType {

  def inner(): String = x
}
