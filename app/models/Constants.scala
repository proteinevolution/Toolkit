package models

import java.io.File


/**
  * Created by lzimmermann on 29.05.16.
  */
trait Constants {

  val SEPARATOR = File.separator
  val jobJSONFileName = "JOB.json"
}


trait ExitCodes {

  val SUCCESS = 0
  val TERMINATED = 143
}