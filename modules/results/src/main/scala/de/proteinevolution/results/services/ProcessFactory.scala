package de.proteinevolution.results.services

import better.files._
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.models.ForwardMode
import play.api.Configuration

import scala.sys.process
import scala.sys.process.Process

private[results] object ProcessFactory {

  def apply(
      resultFile: File,
      jobID: String,
      toolName: String,
      tempFileName: String,
      mode: ForwardMode,
      accString: String,
      db: String,
      basePath: String,
      config: Configuration,
      envConfig: String
  ): process.ProcessBuilder = {

    val generateAlignmentScript = (basePath + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
    val retrieveFullSeq         = (basePath + "/retrieveFullSeq.sh").toFile
    val retrieveAlnEval         = (basePath + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval
    val retrieveFullSeqHHblits  = (basePath + "/retrieveFullSeqHHblits.sh").toFile // why so little abstractions ???

    val (script, params) = (toolName, mode.toString) match {
      case (HHBLITS.value, "alnEval") | (HHPRED.value, "alnEval") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (HMMER.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "count"))
      case (PSIBLAST.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "eval"))
      case (HMMER.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "selHmmer"))
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

    val env: List[(String, String)] = List(
      "ENVIRONMENT"  -> envConfig,
      "BIOPROGSROOT" -> config.get[String]("bioprogs_root"),
      "DATABASES"    -> config.get[String]("db_root")
    )

    Process(script.pathAsString, resultFile.toJava, params ++ env: _*)
  }

}
