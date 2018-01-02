package de.proteinevolution.tools.services

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.ToolNames

import scala.sys.process
import scala.sys.process.Process

private[tools] object ProcessFactory {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
  //private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile
  private val retrieveAlnEval         = (serverScripts + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval

  def apply(resultFile: File,
            jobID: String,
            toolName: String,
            tempFileName: String,
            mode: String,
            evalString: String): process.ProcessBuilder = {

    val (script, params) = (toolName, mode) match {
      case (ToolNames.HHBLITS.value, "alnEval") | (ToolNames.HHPRED.value, "alnEval") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> evalString))
      case (ToolNames.HMMER.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> evalString, "filename" -> tempFileName, "mode" -> "count"))
      case (ToolNames.PSIBLAST.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> evalString, "filename" -> tempFileName, "mode" -> "eval"))
      case _ => throw new IllegalArgumentException("no valid parameters for processing a forwarding job")
    }

    Process(script.pathAsString, resultFile.toJava, params: _*)

  }

}
