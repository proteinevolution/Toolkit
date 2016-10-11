package models.tel.env

import better.files._
import models.tel.Subject

import scala.sys.process.Process




/**
  * Abstract class representing environment files for TEL
  *
  * Created by lzimmermann on 8/19/16.
  */
object EnvFile {


  final val placeholder = "%([A-Z]+)".r("expression")
}
abstract class EnvFile(path : String) extends Subject[EnvFile]{

  final val f = path.toFile
  def load : Map[String, String]

  // Exceptions
  case class EnvFileException(message : String) extends Exception(message)
}




class ExecFile(path : String) extends EnvFile(path) {

  def load : Map[String, String] = {

    if(!f.isExecutable) {
      throw EnvFileException(s"File ${f.name} is not executable.")
    }
    Process(f.pathAsString).!!.lines.map{ line =>

      val spt = line.split('=')
      spt(0).trim() -> spt(1).trim()
    }.toMap
  }
}


/**
  * Represents a Prop file and enables loading
  * its content
  *
 */
class PropFile(path : String) extends EnvFile(path) {


  def load : Map[String, String] = {

      // Remove comment lines and lines containing only whitespace
      this.f.lineIterator
        .map(_.split('#')(0))
        .withFilter(!_.trim().isEmpty)
        .foldLeft(Map.empty[String, String]) { (a, b) =>

          val spt = b.split('=')

          val updated = EnvFile.placeholder.replaceAllIn(spt(1), matcher => a(matcher.group("expression"))).trim()
          a.updated(spt(0).trim(), updated)
        }
  }
}
