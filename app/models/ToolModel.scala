package models

import java.io.File
import models.data.Inport
import play.api.Play._

/**
  * Created by lukas on 1/16/16.
  */
abstract class ToolModel {

  val resultFileNames :  Vector[String]


  /*
  def makeInputForm = {

    val mainMapping =  inports flatMap  {case (inport, no) =>

      for(i <- 0 until no) yield inport.str + no.toString -> inport.pattern
    }
  }
*/


  def resultFilePaths(jobID : Long) : Vector[String] = {

    val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"

    this.resultFileNames map {

      path + File.separator + _
    }
  }
}
