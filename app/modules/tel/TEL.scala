package modules.tel

import java.nio.file.attribute.PosixFilePermission
import javax.inject.{Inject, Singleton}

import scala.sys.process._
import better.files.Cmds._
import better.files._
import models.Implicits._
import modules.tel.env.Env
import modules.tel.param.Params
import modules.tel.runscripts.{ExecutionContext, FileAlreadyExists, Runscript}


/**
  * TEL is the access point to get ExecutionContexts in which runscripts can be executed
  *
  * Created by lzimmermann on 26.05.16.
  */
@Singleton
class TEL @Inject() (env : Env,
                     params: Params) extends TELRegex with TELConstants   {



  def getExecutionContext(root: File, runscript: Runscript) = {

    if(root.exists) {
      throw FileAlreadyExists("ExecutionContext could not be created as the root directory already exists")
    }
    else {

      context match {

        case "LOCAL" => new ExecutionContext(root, root./(executableName), runscript)

        case _ =>

          new ExecutionContext(root, {


          })
      }
    }
  }




  // Ignore the following keys when writing parameters // TODO This is a hack and must be changed
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // Each tool exection consists of the following subdirectories
  val subdirs : Seq[String] = Array("params", "results", "temp", "logs")

  var port = "" // TODO (REMINDER) : REMOVE THIS FOR PRODUCTION !!!

  lazy val context = env.get("CONTEXT")







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
  // Public methods
  //-----------------------------------------------------------------------------------------------------


  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the executable script for job execution.
    */
  def init(runscript : String,  dest : String, params : Option[Map[String, String]] = None): String = {

    val jobID = dest.split('/').last
    // Create directories necessary for tool execution

    // Do not create directories when no params are provided
    if(params.isDefined) {
      subdirs.foreach { s => (dest + SEPARATOR + s).toFile.createDirectories() }

      // Write parameters to file
      for((paramName, value) <- params.get ) {
        // TODO This is a hack and needs to go
        if(!ignore.contains(paramName)) {

          s"$dest${SEPARATOR}params$SEPARATOR$paramName".toFile.write(value)
        }
      }
    }

    val hostname_cmd = "hostname"
    val hostname = hostname_cmd.!!.dropRight(1) // remove trailing whitespace
    val source = s"$runscriptPath$runscript.sh"
    val target = s"$dest$runscript.sh".toFile

    target.append(s"#!/bin/bash\n " +
      s"trap catch_errors ERR;\n" +
      s"function catch_errors() {\n" +
      s"   curl -X POST http://$hostname:$port/jobs/error/$jobID\n " +
      s"  echo 'script aborted, because of errors';\n" +
      s"   exit 0;\n" +
      s"}\n" +
      s"curl -X POST http://$hostname:$port/jobs/running/$jobID\n" +
      s"curl -X POST http://$hostname:$port/jobs/sge/$jobID/$${JOB_ID}\n")


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

    val log_cmds = """
                      | outfile=(*.sh.e*)
                      |errfile=(*.sh.o*)
                      |if [ -a "${outfile[0]}" ];
                      |then
                      |cp *.sh.o* logs/stdout.out
                      |fi
                      |if [ -a "${errfile[0]}" ];
                      |then
                      |cp *.sh.e* logs/stderr.err
                      |fi
                      |""".stripMargin

    target.appendLines(log_cmds)
    target.appendLines(s"curl -X POST http://$hostname:$port/jobs/done/$jobID")


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
      contextFile.appendLines("#!/bin/bash\n")
      //contextFile.appendLines(s"curl -X POST http://$hostname:$port/jobs/queued/$jobID\n")
      contextFile.appendLines(contextLines:_*)
      chmod_+(PosixFilePermission.OWNER_EXECUTE, contextFile)

      s"$dest${SEPARATOR}EXECUTION".toFile.appendLine(contextFile.name)
      contextFile.renameTo("tool.sh")
      contextFile.pathAsString
    }
  }
}

