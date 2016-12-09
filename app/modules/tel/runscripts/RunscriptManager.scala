package modules.tel.runscripts

import javax.inject.{Inject, Named, Singleton}

import better.files._
import modules.tel.TELRegex
import modules.tel.runscripts.Runscript.Evaluation
import play.api.Logger


//TODO
// * Support Constraints and Conditions

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




class Runscript(val parameters: Seq[(String, Evaluation)])  {

  // Implications with names for the parameters // TODO Currently also not supported
  type Condition = (String, RType => Boolean, String, RType => Boolean)

  // Names of the parameters that need to be supplied to that runscript
  final val parameterNames: Seq[String] = parameters.map(_._1).distinct
}


object Runscript extends TELRegex {


  // An evaluation returns an argument given a value for a runscript type and an execution Context
  type Evaluation = (RType, ExecutionContext) => Argument

  // TODO Constraints are not yet supported, currently all arguments are valid
  /**
    * Reads the lines of a runscript file and returns a new runscript instance
    *
    * @param file
    */
  def apply(file: File): Runscript = {

    new Runscript(
      parameterString.findAllIn(file.contentAsString)
        .matchData
        .foldLeft(Seq.empty[(String, Evaluation)]) { (a, m) =>

          val paramName = m.group("paramName")

          a :+ paramName -> { (value: RType, executionContext: ExecutionContext) =>
            m.group("repr") match {
              case "path" =>  ValidArgument(new FileRepresentation(executionContext.getFile(paramName, value.inner().toString)))
              case "content" => ValidArgument(new LiteralRepresentation(value))
            }
          }
          })
        }
}


sealed trait RType {

  def inner(): String
}
case class RString(x : String) extends RType {

  def inner(): String = x
}







