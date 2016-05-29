package models.tools

import models.Constants
import better.files._
import play.api.Logger


/**
  * Created by lzimmermann on 26.05.16.
  */
object Runscript {


  val toolmode_path = "conf/toolmodes"

  private val constants = {

    val path = s"$toolmode_path${Constants.SEP}CONSTANTS"

    path.toFile.lines.map { line =>

      val spt = line.split("=")
      spt(0) -> spt(1)

    }.toMap
  }


  // Returns a map where each tool is associated with a particular toolmode
  private def parseToolmodes(path : String ) :  Map[String, String]  =  {

    path.toFile.lines.withFilter { _.split('#')(0).split("\\s+") == 2 }   .map { line =>

      val spt = line.split("\\s+")
      spt(0)

    }

    null
  }



  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the excutable script for job execution.
    */
  def init(toolname : String, params : Map[String, String]): Unit = {

  // TODO Implement me


  }
}

object TEL_FASTA {


  def nSeq(input : String) = 42
}
