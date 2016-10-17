package models.tel

import java.nio.file.attribute.PosixFilePermission
import javax.inject.{Inject, Singleton}
import play.Play

import scala.sys.process._
import better.files.Cmds._
import better.files._
import models.Implicits._
import models.tel.env.Env
import models.tel.param.Params


import scala.io.Source

/**
  *
  * Created by lzimmermann on 26.05.16.
  */
@Singleton
class TEL @Inject() (env : Env,
                     params: Params) extends TELRegex with TELConstants   {


  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")

  var port = ""


  //-----------------------------------------------------------------------------------------------------
  // Set params (Parameters whose values can be taken from a predefined set)
  //-----------------------------------------------------------------------------------------------------


  /**
    * Returns the Array of all values and plain text names of the set params
    *
    * @param param
    */
  def generateValues(param : String) : Map[String, String] = params.generateValues(param)



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
  // Public methods
  //-----------------------------------------------------------------------------------------------------

  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the executable script for job execution.
    */
  def init(runscript : String, params : Map[String, String], dest : String): String = {

    val jobID = dest.split('/').last
    // Create directories necessary for tool execution
    subdirs.foreach { s => (dest + SEPARATOR + s).toFile.createDirectories() }

    // Write parameters to file
    for((paramName, value) <- params ) {

      // TODO This is a hack and needs to go
      if(!ignore.contains(paramName)) {

        s"$dest${SEPARATOR}params$SEPARATOR$paramName".toFile.write(value)
      }
    }

    val source = s"$runscriptPath$runscript.sh"
    val target = s"$dest$runscript.sh".toFile


    lazy val newLines = source.toFile.lines.map { line =>
      replaceString.replaceAllIn(line, { matcher =>

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
    // add HTTP request POST to update job status to 'Done'
    // TODO write port number dynamically into 'jobStatus*.sh'
    val hostname_cmd = "hostname"
    val hostname = hostname_cmd.!!.dropRight(1)

    target.appendLines(s"cp *.sh.e* logs/stderr.err\ncp *.sh.o* logs/stdout.out\ncurl -X POST http://$hostname:$port/jobs/done/$jobID")


    val context = env.get("CONTEXT")

    if(context == "LOCAL") {


      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(target.name)

      // Make the runscript executable
      chmod_+(PosixFilePermission.OWNER_EXECUTE, target)

      // Rename executable file to tool.sh to make it uniform
      target.renameTo("tool.sh")
      target.pathAsString
    } else {

      // Write the context script to the job directory and insert runscript name
      // TODO The context script should also use the extended syntax
      val contextLines = s"$contextPath$context.sh".toFile.lines.map { line =>

        runscriptString.replaceAllIn(line, s"$runscript.sh")
      }.toSeq

      val contextFile = s"$dest$context.sh".toFile

      contextFile.appendLines(contextLines:_*)

      contextFile.appendLines(s"#!/bin/bash\ntrap catch_errors ERR;\nfunction catch_errors() {\n   curl -X POST http://$hostname:$port/jobs/error/$jobID\n   echo 'script aborted, because of errors';\n   exit 0;\n}\ncurl -X POST http://$hostname:$port/jobs/running/$jobID\n")


      chmod_+(PosixFilePermission.OWNER_EXECUTE, contextFile)

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(contextFile.name)
      contextFile.renameTo("tool.sh")
      contextFile.pathAsString
    }
  }
}

