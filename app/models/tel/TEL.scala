package models.tel

import better.files._
import models.Constants
import play.api.Logger



/**
  * Created by lzimmermann on 26.05.16.
  */
object TEL {

  case class ToolParameter(name : String, t : String)

  // TODO Get this from the configuration
  val TELPath = "tel"
  val typesPath = s"$TELPath${Constants.SEP}types"
  val runscriptPath = s"$TELPath${Constants.SEP}runscripts${Constants.SEP}"


  // For translating the runscript template into an executable instance
  val replaceeString = """%([A-Za-z_\.]+)""".r("expression")

  // Elements of the markup of runscripts, currently constants and parameter string are supported
  val constantsString =  """([A-Z]+)""".r("constant")
  val parameterString = """([a-z_]+)\.([a-z_]+)""".r("paramName", "selector")

  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")

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
    *  returns the name of the executable script for job execution.
    */
  def init(toolname : String, params : Map[String, String], dest : String): Unit = {

    // Create directories necessary for tool execution
    subdirs.foreach { s => (dest + Constants.SEP + s).toFile.createDirectories() }

    // Write parameters to file
    for((paramName, value) <- params ) {

      if(! ignore.contains(paramName)) {

        s"$dest${Constants.SEP}params${Constants.SEP}$paramName".toFile.write(value)
      }
    }
    val source = s"$runscriptPath$toolname.sh"
    val target = s"$dest$toolname.sh".toFile

    lazy val newLines = source.toFile.lines.map { line =>

      replaceeString.replaceAllIn(line, { matcher =>

          matcher.group("expression").trim() match {

          // Replace constants
          case constantsString(constant) =>
            Logger.info("Constant: " + constant)
            constants(constant)

          // Replace param String
          case parameterString(paramName, selector) =>

            Logger.info("paramName: " + paramName)
            Logger.info("selector: " + selector)
            // Some selectors hard-coded TODO Introduce the extensions of selectors with arbitrary methods
            selector match {
              case "path" => s"params${Constants.SEP}$paramName"
              case "content" =>
                s"${dest}params${Constants.SEP}$paramName".toFile.contentAsString
            }


          // Should not happen
          case _ => "notImplemented"

        }
      })
    }.toSeq

    target.appendLines(newLines:_*)
  }


}

