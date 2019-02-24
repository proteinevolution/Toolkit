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

package de.proteinevolution.results.results

import java.nio.file.{ Files, Paths }

import play.twirl.api.Html

import better.files._

object HHrepID {

  def getResult(jobID: String, filePath: String): Html = {
    val headerLine = """(Results for repeats type )([A-Z])(:)""".r
    val seqLine    = """([\S]+\s+[\S]+\s+[\S]+\s+[\S]+\s)([\S]+)""".r
    val imagePath  = s"/results/files/$jobID/query_"
    val data = (for { in <- File(filePath).newInputStream.autoClosed } yield
      in.lines.toList.map {
        case wholeMatch @ headerLine(_, m, _) =>
          "<h5>" + wholeMatch + "</h5>" + "<span class='hhrepImage'>" +
          s"<img hspace='14' src='$imagePath$m.png'>" + "</div><br />"
        case wholeMatch @ seqLine(_, m) =>
          "<pre class='sequence hhrepidview'>" + wholeMatch.replace(m, Common.colorRegexReplacer(m)) + "</pre>"
        case "" => "<br />"
        case m  => "<pre class='sequence hhrepidview'>" + m + "</pre>"
      }).get()
    Html(data.mkString(""))
  }

  def existsResult(filePath: String): Boolean = {
    Files.exists(Paths.get(filePath))
  }

}
