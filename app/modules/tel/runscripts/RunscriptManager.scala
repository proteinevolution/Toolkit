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
  *
  * Created by lzimmermann on 10/19/16.
  */
@Singleton
class RunscriptManager @Inject() (@Named("runscriptPath") runscriptPath : String)  {

  Logger.info(s"RunscriptManager started, surveilling: $runscriptPath")

  val runscripts = runscriptPath.toFile.list.withFilter(_.extension.getOrElse("") == ".sh").map { file =>

    file.name -> Runscript(file)
  }.toMap

  def apply(runscriptName: String) = runscripts(runscriptName)
}


// An argument is either valid or invalid. If it is invalid, it can be represented, otherwise it has
// a collection of violated Constraints
sealed trait Argument
case class InvalidArgument(violatedConstraints: Seq[RType => Boolean]) extends Argument
case class ValidArgument(representation: Representation) extends Argument




class Runscript(val parameters: Map[String, Seq[Evaluation]])  {

  // Implications with names for the parameters // TODO Currently also not supported
  type Condition = (String, RType => Boolean, String, RType => Boolean)
}


object Runscript extends TELRegex {


  // An evaluation within a runscript maps a RType to a Argument(which indicates whether the evalaution was
  // successful or not)
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
        .foldLeft(Map.empty[String, Seq[Evaluation]].withDefaultValue(Seq.empty)) { (a, m) =>

          val spt = m.split('.')
          val paramName = spt(0)

          a.updated(paramName, a(paramName) :+ { (value: RType, executionContext: ExecutionContext) =>

            spt(1) match {

              case "path" => ValidArgument(FileRepresentation(paramName, value, executionContext))
              case "content" => ValidArgument(LiteralRepresentation(value, executionContext))
            }
          }
          )
        })
  }
}

sealed trait RType
case class StringType(x : String) extends RType







