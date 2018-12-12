package de.proteinevolution.tel.env

import better.files._
import de.proteinevolution.tel.Subject
import play.api.{ Configuration, Logger }

import scala.sys.process.Process
import scala.util.matching.Regex

object EnvFile {

  final val placeholder: Regex = "%([a-z]+)".r("expression")

}

abstract class EnvFile(path: String) extends Subject[EnvFile] {

  final val f: File = path.toFile
  def load: Map[String, String]

  // Exceptions
  case class EnvFileException(message: String) extends Exception(message)
}

class ExecFile(path: String) extends EnvFile(path) {

  def load: Map[String, String] = {

    if (!f.isExecutable) {
      throw EnvFileException(s"File ${f.name} is not executable.")
    }
    // ambiguous implicit conversion because of method `lines` in better.files.package.StringOps
    Process(f.pathAsString).!!.lines.map { line =>
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
class PropFile(path: String, config: Configuration) extends EnvFile(path) {

  private val logger = Logger(this.getClass)

  def load: Map[String, String] = {
    // Remove comment lines and lines containing only whitespace
    this.f.lineIterator.map(_.split('#')(0)).withFilter(!_.trim().isEmpty).foldLeft(Map.empty[String, String]) {
      (a, b) =>
        val spt     = b.split('=')
        var updated = EnvFile.placeholder.replaceAllIn(spt(1), matcher => a(matcher.group("expression"))).trim()
        updated match {
          case x if x.startsWith("foo") => updated = updated.replace("foo", config.get[String]("db_root"))
          case x if x.startsWith("env_foo") =>
            updated = updated.replace("env_foo", config.get[String]("environment"))
          case x if x.startsWith("helper_foo") =>
            updated = updated.replace("helper_foo", config.get[String]("helper_scripts"))
          case x if x.startsWith("perllib_foo") =>
            updated = updated.replace("perllib_foo", config.get[String]("perl_lib"))
          case x if x.startsWith("standarddb_bar") =>
            updated = updated.replace("standarddb_bar", config.get[String]("standard_db"))
          case _ => logger.debug("Env file has no preconfigured key in the configs")
        }
        a.updated(spt(0).trim(), updated)
    }
  }
}
