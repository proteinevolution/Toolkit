package de.proteinevolution.tools.services

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.ToolName
import ToolName._
import de.proteinevolution.tools.models.ForwardMode

import scala.sys.process
import scala.sys.process.Process

private[tools] object ProcessFactory {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
  private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeq.sh").toFile
  private val retrieveAlnEval         = (serverScripts + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval
  private val retrieveFullSeqHHblits  = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile // why so little abstractions ???

  def apply(resultFile: File,
            jobID: String,
            toolName: String,
            tempFileName: String,
            mode: ForwardMode,
            accString: String,
            db: String): process.ProcessBuilder = {

    val (script, params) = (toolName, mode.toString) match {
      case (HHBLITS.value, "alnEval") | (HHPRED.value, "alnEval") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (HMMER.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "count"))
      case (PSIBLAST.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "eval"))
      case (HMMER.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "selHmmer")) // todo refactor! index issue because of no query in file
      case (PSIBLAST.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "sel"))
      case (HHPRED.value, "aln") | (HHBLITS.value, "aln") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (PSIBLAST.value, "evalFull") | (HMMER.value, "evalFull") =>
        (retrieveFullSeq, List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case (HHBLITS.value, "evalFull") | (HHBLITS.value, "full") =>
        (retrieveFullSeqHHblits,
         List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case (PSIBLAST.value, "full") | (HMMER.value, "full") =>
        (retrieveFullSeq, List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case _ => throw new IllegalArgumentException("no valid parameters for processing a forwarding job")
    }
    Process(script.pathAsString, resultFile.toJava, params: _*)
  }

}
