package models.tel

import java.nio.file.attribute.PosixFilePermission
import javax.inject.{Inject, Singleton}

import better.files.Cmds._
import better.files._

import scala.sys.process._
import models.Implicits._
import models.tel.env.Env


/**
  *
  * Created by lzimmermann on 26.05.16.
  */
@Singleton
class TEL @Inject() (env : Env) extends TELRegex with TELConstants   {


  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")


  //-----------------------------------------------------------------------------------------------------
  // Set params (Parameters whose values can be taken from a predefined set)
  //-----------------------------------------------------------------------------------------------------

  // Keeps a map of all setParams with their respective allowed values, together with the plain text name
  private var setParams : Map[String, Map[String, String]] = loadSetParams()

  // Reloads all the set Params from the scripts in params.d
  private def loadSetParams() = {

    // Only consider .sh and .dat files in the params.d directory
   paramsDFile.list
     .withFilter( f => f.isRegularFile && f.hasExtension)  // Ensure that only regular files with extension are used
     .withFilter { f =>
     val ext = f.extension.get
     ext == ".sh" || ext == ".dat"    // Only ".sh" files and ".dat" files are considered
   }.map { f =>

      f.name.replaceAll("(.sh|.dat)", "") ->   { f.extension.get match {

        case ".sh"   =>  Process(f.pathAsString).!!.split('\n').map { param =>
          val spt = param.split(' ')
          spt(0) -> spt(1)
        }.toMap

        case ".dat" => f.lineIterator.map { line =>
          val spt = line.split(' ')
          spt(0) -> spt(1)
        }.toMap
      }
      }
      }
    }.toMap




  /**
    * Returns the Array of all values and plain text names of the set params
    *
    * @param param
    */
  def getSetParam(param : String) = {

    // TODO Reload contents of params.d if changed
    // You probably want to use load set params here

    setParams(param)
  }



  //-----------------------------------------------------------------------------------------------------
  // Types
  //-----------------------------------------------------------------------------------------------------

  private var types : Map[String, Array[String]] = loadTypes()

  // Reloads all the set Params from the scripts in params.d
  private def loadTypes() = {
    typesFile
      .lineIterator
      .withoutComment(commentChar)
      .noWSLines
      .map { line =>

        val spt = line.split(":")
        spt(0).trim() -> spt(1).split("\\s+")
      }.toMap
  }



  //-----------------------------------------------------------------------------------------------------
  // Other stuff
  //-----------------------------------------------------------------------------------------------------

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



  //-----------------------------------------------------------------------------------------------------
  // Private methods
  //-----------------------------------------------------------------------------------------------------

  /**
    *
    *
    *
    */
  def parseParams(): Unit = {


  }




  //-----------------------------------------------------------------------------------------------------
  // Public methods
  //-----------------------------------------------------------------------------------------------------

  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the executable script for job execution.
    */
  def init(runscript : String, params : Map[String, String], dest : String): String = {

    // Create directories necessary for tool execution
    subdirs.foreach { s => (dest + SEPARATOR + s).toFile.createDirectories() }

    // Write parameters to file
    for((paramName, value) <- params ) {

      // TODO This is a hack and needs to go
      if(! ignore.contains(paramName)) {

        s"$dest${SEPARATOR}params$SEPARATOR$paramName".toFile.write(value)
      }
    }

    //
    val source = s"$runscriptPath$runscript.sh"
    val target = s"$dest$runscript.sh".toFile

    lazy val newLines = source.toFile.lines.map { line =>

      replaceeString.replaceAllIn(line, { matcher =>

          matcher.group("expression").trim() match {

          // Replace constants
          case constantsString(constant) => env.get(constant)

          // Replace param String
          case parameterString(paramName, selector) =>

            // Some selectors hard-coded TODO Introduce the extensions of selectors with arbitrary methods
            selector match {
              case "path" => s"params$SEPARATOR$paramName"
              case "content" =>
                s"${dest}params$SEPARATOR$paramName".toFile.contentAsString
            }

          // Should not happen
          case _ => "notImplemented"

        }
      })
    }.toSeq
    target.appendLines(newLines:_*)

    val context = env.get("CONTEXT")
    if(context == "LOCAL") {

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(target.name)

      // Make the runscript executable
      chmod_+(PosixFilePermission.OWNER_EXECUTE, target)

      target.pathAsString
    } else {

      // Write the context script to the job directory and insert runscript name
      // TODO The context script should also use the extended syntax
      val contextLines = s"$contextPath$context.sh".toFile.lines.map { line =>

        runscriptString.replaceAllIn(line, s"$runscript.sh")
      }.toSeq

      val contextFile = s"$dest$context.sh".toFile

      contextFile.appendLines(contextLines:_*)
      chmod_+(PosixFilePermission.OWNER_EXECUTE, contextFile)

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(contextFile.name)
      contextFile.pathAsString
    }
  }
}

