package models.tools

import java.io.File


import com.typesafe.config.ConfigFactory


/**
  * Created by lukas on 1/16/16.
  */
abstract class ToolModel {


//   val inport : Inport[AnyRef]

  val toolNameShort:String        // short version of the name of the tool
  val toolNameLong:String         // long version of the name of the tool
  val toolNameAbbreviation:String // abbreviation of the name of the tool

  val resultFileNames :  Vector[String]

  // Returns the Input Form Definition of this tool
  //val inputForm:Form[Product with Serializable]
  /*
     TODO: Type is unknown type of Form[T] and will give errors in Tool.scala when changed...
    overriding value inputForm in class ToolModel of type play.api.data.Form[Product with Serializable];
    value inputForm has incompatible type
    */


  /*
  def makeInputForm = {

    val mainMapping =  inports flatMap  {case (inport, no) =>

      for(i <- 0 until no) yield inport.str + no.toString -> inport.pattern
    }
  }
  */



  def resultFilePaths(jobID : Long) : Vector[String] = {

    val path = s"${ConfigFactory.load().getString("job_path")}${File.separator}$jobID"

    this.resultFileNames map {

      path + File.separator + _
    }
  }
}
