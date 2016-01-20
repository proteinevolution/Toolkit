package models

import java.io.File
import play.api.Play._

/**
  * Created by lukas on 1/16/16.
  */
abstract class ToolModel {

  // Specifies the Command line invocation of the tool
  // TODO Of course, this needs to be generalized
  val exec: Vector[CallComponent]


  val resultFileNames :  Vector[String]


  def resultFilePaths(jobID : Long) : Vector[String] = {

    val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"

    this.resultFileNames map {

      path + File.separator + _
    }
  }
}
