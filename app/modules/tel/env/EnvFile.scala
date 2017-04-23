package modules.tel.env

import better.files._
import com.typesafe.config.ConfigFactory
import modules.tel.Subject
import play.api.Logger

import scala.sys.process.Process
import scala.util.matching.Regex



/**
  * Abstract class representing environment files for TEL
  *
  * Created by lzimmermann on 8/19/16.
  */
object EnvFile {


  final val placeholder : Regex = "%([A-Z]+)".r("expression")
}
abstract class EnvFile(path : String) extends Subject[EnvFile]{

  final val f : File = path.toFile
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

          var updated = EnvFile.placeholder.replaceAllIn(spt(1), matcher => a(matcher.group("expression"))).trim()


          updated match {

            case x if x.startsWith("foo") => updated = updated.replace("foo", ConfigFactory.load().getString("DBROOT"))
            case x if x.startsWith("env_foo") => updated = updated.replace("env_foo", ConfigFactory.load().getString("ENVIRONMENT"))
            case x if x.startsWith("helper_foo") => updated = updated.replace("helper_foo", ConfigFactory.load().getString("HELPER"))
            case x if x.startsWith("perllib_foo") => updated = updated.replace("perllib_foo", ConfigFactory.load().getString("PERLLIB"))
            case _ => Logger.info("Env file has no preconfigured key in the configs")

          }

          a.updated(spt(0).trim(), updated)

        }
  }
}
