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