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

sealed trait ResultForm {
  def start: Int
  def end: Int
  def copy(end: Int): ResultForm
}

object ResultForm {

  implicit def rw: RW[ResultForm] = RW.merge(ShowHitsForm.rw, MsaResultForm.rw, ClustalResultForm.rw)

  case class ShowHitsForm(start: Int, end: Int, wrapped: Boolean, isColor: Boolean) extends ResultForm {
    override def copy(newEnd: Int): ShowHitsForm = new ShowHitsForm(start, newEnd, wrapped, isColor)
  }

  object ShowHitsForm {

    implicit def rw: RW[ShowHitsForm] = macroRW

  }

  case class MsaResultForm(start: Int, end: Int, resultName: String) extends ResultForm {
    override def copy(newEnd: Int): MsaResultForm = new MsaResultForm(start, newEnd, resultName)
  }

  object MsaResultForm {

    implicit def rw: RW[MsaResultForm] = macroRW

  }

  case class ClustalResultForm(start: Int, end: Int, color: Boolean, resultName: String) extends ResultForm {
    override def copy(newEnd: Int): ClustalResultForm = new ClustalResultForm(start, newEnd, color, resultName)
  }

  object ClustalResultForm {

    implicit def rw: RW[ClustalResultForm] = macroRW

  }

}
