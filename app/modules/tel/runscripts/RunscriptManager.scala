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
class RunscriptManager @Inject() (@Named("runscriptPath") runscriptPath : String)  {

  Logger.info(s"RunscriptManager started, surveilling: $runscriptPath")

  val runscripts : Map[String, Runscript] = runscriptPath.toFile.list.withFilter(_.extension.getOrElse("") == ".sh").map { file =>

    file.nameWithoutExtension -> Runscript(file)
  }.toMap

  def apply(runscriptName: String): Runscript = runscripts(runscriptName)
}


// An argument is either valid or invalid. If it is invalid, it can be represented, otherwise it has
// a collection of violated Constraints
sealed trait Argument
case class InvalidArgument(violatedConstraints: Seq[RType => Boolean]) extends Argument
case class ValidArgument(representation: Representation) extends Argument



sealed trait RType {

  def inner(): String
}
case class RString(x : String) extends RType {

  def inner(): String = x
}







