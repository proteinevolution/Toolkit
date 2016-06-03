package models.tel

import better.files._
import models.Constants

import scala.util.matching.Regex


/**
  * Created by lzimmermann on 26.05.16.
  */
object TEL {

  case class ToolParameter(name : String, t : String)


  // TODO Get this from the configuration
  val TELPath = "TEL"
  val typesPath = s"$TELPath${Constants.SEP}types"
  val runscriptPath = s"$TELPath${Constants.SEP}runscripts${Constants.SEP}"


  val commentChar = '#'

  // For translating the runscript template into an actual executable runscript
  val argumentPattern = new Regex("""%([A-Z]+)""", "expression")


  // Ignore the following keys when writing parameters
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  private val constants = {

    s"$TELPath${Constants.SEP}CONSTANTS".toFile.lines.withFilter { _.trim() != ""  }.map { line =>

      val spt = line.split("=")
      spt(0).trim() -> spt(1).trim()

    }.toMap
  }



  /**
    * Uses reflection to invoke the Method
    *
    * @param methodname
    * @param t
    * @param content
    */
  def invoke(methodname : String, t : String, content : String): Unit = {

    val clazz = Class.forName("models.tel.TEL_" + t )

    // TODO Implement me

  }



  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the excutable script for job execution.
    */
  def init(toolname : String, params : Map[String, String], dest : String): Unit = {

    val source = s"$runscriptPath$toolname.sh"
    val target = s"$dest$toolname.sh".toFile

    val newLines = source.toFile.lines.map { line =>

      argumentPattern.replaceAllIn(line.split(commentChar)(0), { m =>

        val expr = m.group("expression")

        constants(expr)

      } )
    }.toSeq
    target.appendLines(newLines:_*)


  }


}

