package de.proteinevolution.tools.services

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.ToolNames

import scala.sys.process
import scala.sys.process.Process

private[tools] object ProcessFactory {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
  private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile
  private val retrieveAlnEval         = (serverScripts + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval
  private val retrieveFullSeqHHblits  = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile // why so little abstractions ???

  def apply(resultFile: File,
            jobID: String,
            toolName: String,
            tempFileName: String,
            mode: String,
            accString: String,
            db: String): process.ProcessBuilder = {

    val (script, params) = (toolName, mode) match {
      case (ToolNames.HHBLITS.value, "alnEval") | (ToolNames.HHPRED.value, "alnEval") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (ToolNames.HMMER.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "count"))
      case (ToolNames.PSIBLAST.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "eval"))
      case (ToolNames.HMMER.value, "aln") | (ToolNames.PSIBLAST.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "sel"))
      case (ToolNames.HHPRED.value, "aln") | (ToolNames.HHBLITS.value, "aln") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (ToolNames.PSIBLAST.value, "evalFull") | (ToolNames.HMMER.value, "evalFull") =>
        (retrieveFullSeq, List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case (ToolNames.HHBLITS.value, "evalFull") =>
        (retrieveFullSeqHHblits,
         List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case _ => throw new IllegalArgumentException("no valid parameters for processing a forwarding job")
    }

    Process(script.pathAsString, resultFile.toJava, params: _*)

  }

}
