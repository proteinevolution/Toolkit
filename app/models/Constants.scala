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



}