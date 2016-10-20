package models

import java.io.File
import com.typesafe.config.ConfigFactory


/**
  * Created by lzimmermann on 29.05.16.
  */
trait Constants {

  val SEPARATOR = File.separator
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val jobJSONFileName = "JOB.json"
}


trait ExitCodes {

  val SUCCESS = 0
  val TERMINATED = 143
}

object Param {

  final val ALIGNMENT = "alignment"
  final val ALIGNMENT_FORMAT = "alignment_format"
  final val STANDARD_DB = "standarddb"
  final val MATRIX = "matrix"
  final val NUM_ITER = "num_iter"
  final val EVALUE = "evalue"
  final val GAP_OPEN = "gap_open"
  final val GAP_EXT = "gap_ext"
  final val GAP_TERM = "gap_term"
  final val DESC = "desc"
  final val CONSISTENCY = "consistency"
  final val ITREFINE = "itrefine"
  final val PRETRAIN = "pretrain"
  final val MAXROUNDS = "maxrounds"
  final val OFFSET = "offset"
  final val BONUSSCORE = "bonusscore"
  final val OUTORDER = "outorder"
  final val ETRESH = "inclusion_ethresh"
}