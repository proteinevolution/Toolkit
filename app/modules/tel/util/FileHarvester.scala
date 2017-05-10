package modules.tel.util
import better.files.File

import scala.sys.process.Process

/**
  * Generates information from files as String qualified by the Path
  * Created by lukas on 12/29/16.
  */
sealed abstract class FileHarvester(file: File) {

  def apply(): Iterator[String]
}

/**
  *
  *  Just returns the content of the file as line iterator
  * @param file
  */
case class FileContentHarvester(file: File) extends FileHarvester(file) {

  override def apply(): Iterator[String] = file.lineIterator
}

/**
  *
  * Executes File  and returns lines of standard out as iterator
  *
  * @param file
  * @param maxApply
  */
case class FileExecuterHarvester(file: File, maxApply: Int) extends FileHarvester(file) {

  private var n = 0

  override def apply(): Iterator[String] = {

    val x = Process(file.pathAsString).!!.split('\n').toIterator
    this.n += 1

    if (this.n == maxApply) {

      file.delete(swallowIOExceptions = true)
    }
    x
  }
}
