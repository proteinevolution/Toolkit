package modules.tel.runscripts


import better.files._
import modules.tel.TELRegex
import modules.tel.env.Env
import modules.tel.execution.ExecutionContext
import modules.tel.runscripts.Runscript.Evaluation

import scala.collection.mutable
import scala.util.matching.Regex

/**
  * Created by lzimmermann on 10/19/16.
  */


/**
  * Represents one particular runscript, specified by the path of the corresponding file.
  * Instances should be created via the companion object.
  *
  */
class Runscript(val parameters: Seq[(String, Evaluation)], val file: File) extends TELRegex  {

  // Implications with names for the parameters // TODO Currently not supported
  type Condition = (String, RType => Boolean, String, RType => Boolean)

  final val name: String = file.nameWithoutExtension // Name of the Runscript
  final val parameterNames: Seq[String] = parameters.map(_._1).distinct   // Names of the parameters that need to be supplied


  private case class Replacer(arguments : Seq[(String, ValidArgument)]) {
    private var counter = -1

    def apply(m : Regex.Match) : String = {
      counter += 1
      arguments(counter)._2.representation.represent
    }
  }

  private val tranlationSteps = mutable.Queue[String => String]( _ => file.contentAsString)

  // Translates A sequence of Arguments with the parameter names into a runnable runscript instance
  def apply(arguments: Seq[(String, ValidArgument)]): String = {

    // Dequeue all transformers
    var init = tranlationSteps.dequeue()("")
    while(tranlationSteps.nonEmpty) {

      init = tranlationSteps.dequeue()(init)
    }

    val replacer = Replacer(arguments)
    parameterString.replaceAllIn(init, replacer.apply _)
  }

  def withEnvironment(env: Env): Runscript = {

    tranlationSteps.enqueue {   s =>
      constantsString.replaceAllIn(s, m => env.get(m.group("constant")))
    }
    this
  }
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
        }, file)
  }
}
