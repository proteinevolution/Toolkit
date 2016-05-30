package models.tel

import better.files._
import models.Constants


/**
  * Created by lzimmermann on 26.05.16.
  */
object TEL {

  case class ToolParameter(name : String, t : String)


  // TODO Get this from the configuration
  val toolmode_path = "TEL"
  val types_path = s"$toolmode_path${Constants.SEP}types"


  private val constants = {

    s"$toolmode_path${Constants.SEP}CONSTANTS".toFile.lines.map { line =>

      val spt = line.split("=")
      spt(0) -> spt(1)

    }.toMap
  }



  /**
    * Uses reflection to invoke the Method
    *
    * @param methodname
    * @param t
    * @param content
    */
  def invoke(methodname : String, t : String, content : String): Unit = {

    val clazz = Class.forName("models.tel.TEL_" + t )
    clazz


  }






  /**
    *  Assembles all scripts to create a new executable Job and
    *  returns the name of the excutable script for job execution.
    */
  def init(toolname : String, params : Map[String, String]): Unit = {

  // TODO Implement me


  }
}

object FASTA {


  def nSeq(input : String) = 42
}
