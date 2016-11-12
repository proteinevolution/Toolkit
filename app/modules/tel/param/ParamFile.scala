package modules.tel.param

import better.files._
import models.Implicits._


/**
  Provides methods to read Generative Params from a file
 */
object GenerativeParamFileParser {


  def read(filePath : String) : Iterator[GenerativeParam] = {

    val f = filePath.toFile

    f.lineIterator.noWSLines.map { line =>

      val spt = line.split(' ')
      val fileending = spt(2).substring(spt(2).lastIndexOf('.'))

      (spt(1), fileending) match {

        case ("GEN", ".sh") =>

          // Decide for path of the parameter file
          val paramPath = if(spt(2).startsWith("/")) {
            spt(2)
          } else {
            s"${f.parent.pathAsString}/${spt(2)}"
          }
          new ExecGenParamFile(spt(0), paramPath)
      }
    }
  }
}



/*
 * Parameters obtained from files
 */
abstract class GenerativeParamFile(name: String, path : String) extends GenerativeParam(name) {

  /* Load the parameters from the file */
  def load() : Unit
}


class ExecGenParamFile(name : String,  path : String) extends GenerativeParamFile(name, path) {

  import scala.sys.process.Process
  // Load file upon instantiation
  this.load()

  // Remembers parameter values that are allowed to be used
  private var allowed : Set[String] = _
  private var clearTextNames : Map[String, String] = _

  def load() : Unit = {
    clearTextNames = Map.empty

    this.allowed = Process(path).!!.split('\n').map { param =>
      val spt = param.split(' ')
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }

  def generate = this.allowed
  def generateWithClearText = this.clearTextNames
}

class ListGenParamFile(name : String, path : String) extends GenerativeParamFile(name, path) {

  // Load file upon instantiation
  this.load()

  // Remembers parameter values that are allowed to be used
  private var allowed : Set[String] = _
  private var clearTextNames : Map[String, String] = _

  private val f = path.toFile


  def load() : Unit = {
    clearTextNames = Map.empty

    this.allowed = f.lineIterator.map { line =>
      val spt = line.split(' ')
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }

  def generate = this.allowed
  def generateWithClearText = this.clearTextNames
}

