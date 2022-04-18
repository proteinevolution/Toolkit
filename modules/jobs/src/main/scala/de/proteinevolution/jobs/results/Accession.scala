/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

package de.proteinevolution.jobs.results

import simulacrum._

@typeclass trait Accession[A] {
  def value(a: A): String
}

object Accession {
  implicit val hmmerValue: Accession[HmmerHSP]       = (a: HmmerHSP) => a.accession
  implicit val hhpredValue: Accession[HHPredHSP]     = (a: HHPredHSP) => a.template.accession
  implicit val hhblitsValue: Accession[HHBlitsHSP]   = (a: HHBlitsHSP) => a.template.accession
  implicit val hhompValue: Accession[HHompHSP]       = (a: HHompHSP) => a.template.accession
  implicit val psiBlastValue: Accession[PSIBlastHSP] = (a: PSIBlastHSP) => a.accession
}
