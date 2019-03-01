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

package exports.results.models

import upickle.default.{ macroRW, ReadWriter => RW }

sealed trait ForwardingForm

object ForwardingForm {

  implicit def rw: RW[ForwardingForm] = RW.merge(ForwardingFormNormal.rw, ForwardingFormAln.rw)

  case class ForwardingFormNormal(fileName: String, evalue: String, checkboxes: Array[Int]) extends ForwardingForm

  object ForwardingFormNormal {

    implicit def rw: RW[ForwardingFormNormal] = macroRW

  }

  case class ForwardingFormAln(resultName: String, checkboxes: Array[Int]) extends ForwardingForm

  object ForwardingFormAln {

    implicit def rw: RW[ForwardingFormAln] = macroRW

  }

}
