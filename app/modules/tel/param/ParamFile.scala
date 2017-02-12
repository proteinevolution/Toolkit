package modules.tel.param

import java.nio.file.attribute.PosixFilePermission
import javax.inject.{Inject, Singleton}

import better.files._
import models.Implicits._
import modules.tel.env.Env
import play.api.Logger


/**
  Provides methods to read Generative Params from a file
 */

@Singleton
class GenerativeParamFileParser @Inject() (env: Env) {

  private final val genKeyword = "GEN" // Denotes the parameter in the descriptor file as generative


  def read(filePath : String) : Iterator[GenerativeParam] = {

    val f = filePath.toFile

    f.lineIterator.noWSLines.map { line =>

      val spt = line.split("\\s+")

      // Decide for path of the parameter file
      val paramPath = if(spt(2).startsWith("/")) {
        spt(2)
      } else {
        s"${f.parent.pathAsString}/${spt(2)}"
      }
      (spt(1), spt(2).substring(spt(2).lastIndexOf('.'))) match {
        case (this.genKeyword, ".sh") => new ExecGenParamFile(spt(0), paramPath).withEnvironment(env)
        case (this.genKeyword, ".py") => new ExecGenParamFile(spt(0), paramPath).withEnvironment(env)
        case (this.genKeyword, ".prop") => new ListGenParamFile(spt(0), paramPath).withEnvironment(env)
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

  private var env: Option[Env] = None
  import scala.sys.process.Process

  override def withEnvironment(env: Env):ExecGenParamFile = {
    this.env = Some(env)
    this.load()
    this
  }

  // Remembers parameter values that are allowed to be used
  private var allowed : Set[String] = _
  private var clearTextNames : Map[String, String] = _

  def load() : Unit = {
    clearTextNames = Map.empty
    val lines = this.env match {
      case Some(e) =>
        val tempFile = File.newTemporaryFile()
        tempFile.setPermissions(Set(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ))
        tempFile.write(envString.replaceAllIn(path.toFile.contentAsString, m => e.get(m.group("constant"))))
        val x = Process(tempFile.pathAsString).!!.split('\n')
        tempFile.delete(swallowIOExceptions = true)
        x
      case None =>  Process(path).!!.split('\n')
    }

    this.allowed = lines.map { param =>
      val spt = param.split("\\s+")
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }
  def generate: Map[String, String] = this.clearTextNames
}

class ListGenParamFile(name : String, path : String) extends GenerativeParamFile(name, path) {

  private val f = path.toFile

  override def withEnvironment(env: Env) : ListGenParamFile = this

  // Load file upon instantiation
  this.load()

  // Remembers parameter values that are allowed to be used
  private var allowed : Set[String] = _
  private var clearTextNames : Map[String, String] = _


  def load() : Unit = {
    clearTextNames = Map.empty

    this.allowed = f.lineIterator.map { line =>

      Logger.info("Reading line " + line)
      val spt = line.split("\\s+")
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }

  def generate : Map[String, String] = this.clearTextNames
}

