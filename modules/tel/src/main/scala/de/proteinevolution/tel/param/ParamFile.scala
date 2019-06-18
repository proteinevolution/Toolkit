/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.tel.param

import java.nio.file.attribute.PosixFilePermission

import javax.inject.{Inject, Singleton}
import better.files._
import de.proteinevolution.tel.param.Implicits._
import play.api.Configuration

import scala.collection.immutable.ListMap

/**
  Provides methods to read Generative Params from a file
 */
@Singleton
class GenerativeParamFileParser @Inject()(config: Configuration) {

  private final val genKeyword = "GEN" // Denotes the parameter in the descriptor file as generative

  def read(filePath: String): Iterator[GenerativeParam] = {

    val f = filePath.toFile
    val env = config.get[Map[String, String]]("tel.env")

    f.lineIterator.noWSLines.map { line =>
      val spt = line.split("\\s+")

      // Decide for path of the parameter file
      val paramPath = if (spt(2).startsWith("/")) {
        spt(2)
      } else {
        s"${f.parent.pathAsString}/${spt(2)}"
      }
      (spt(1), spt(2).substring(spt(2).lastIndexOf('.'))) match {
        case (this.genKeyword, ".sh")   => new ExecGenParamFile(spt(0), paramPath).withEnvironment(env)
        case (this.genKeyword, ".py")   => new ExecGenParamFile(spt(0), paramPath).withEnvironment(env)
        case (this.genKeyword, ".prop") => new ListGenParamFile(spt(0), paramPath).withEnvironment(env)
        case _                          => throw new IllegalStateException("no valid paramfile extension found. Must be .sh, .py, or .prop")
      }
    }
  }
}

/*
 * Parameters obtained from files
 */
abstract class GenerativeParamFile(name: String) extends GenerativeParam(name) {

  /* Load the parameters from the file */
  def load(): Unit
}

class ExecGenParamFile(name: String, path: String, private var allowed: Set[String] = Set.empty[String])
    extends GenerativeParamFile(name) {

  private var env: Option[Map[String, String]] = None
  import scala.sys.process.Process

  def withEnvironment(env: Map[String, String]): ExecGenParamFile = {
    this.env = Some(env)
    this.load()
    this
  }

  private var clearTextNames: ListMap[String, String] = _

  def load(): Unit = {
    clearTextNames = ListMap.empty
    val lines = this.env match {
      case Some(e) =>
        val tempFile = File.newTemporaryFile()
        tempFile.setPermissions(
          Set(
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_WRITE
          )
        )
        tempFile.write(envString.replaceAllIn(path.toFile.contentAsString, m => e.getOrElse(m.group("constant"), "")))
        val x = Process(tempFile.pathAsString).!!.split('\n')
        tempFile.delete(swallowIOExceptions = true)
        x
      case None => Process(path).!!.split('\n')
    }

    allowed = lines.map { param =>
      val spt = param.split("\\s+")
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }
  def generate: ListMap[String, String] = this.clearTextNames
}

class ListGenParamFile(name: String, path: String, private var allowed: Set[String] = Set.empty[String])
    extends GenerativeParamFile(name) {

  private val f = path.toFile

  def withEnvironment(env: Map[String, String]): ListGenParamFile = this

  // Load file upon instantiation
  load()

  private var clearTextNames: ListMap[String, String] = _

  def load(): Unit = {
    clearTextNames = ListMap.empty

    allowed = f.lineIterator.map { line =>
      val spt = line.split("\\s+")
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }

  def generate: ListMap[String, String] = this.clearTextNames
}
