package exports.results.models

import upickle.default.{ macroRW, ReadWriter => RW }

sealed trait ResultForm {
  var start: Int
  var end: Int
}

object ResultForm {

  implicit def rw: RW[ResultForm] = RW.merge(ShowHitsForm.rw, MsaResultForm.rw, ClustalResultForm.rw)

  case class ShowHitsForm(var start: Int, var end: Int, wrapped: Boolean, isColor: Boolean) extends ResultForm

  object ShowHitsForm {

    implicit def rw: RW[ShowHitsForm] = macroRW

  }

  case class MsaResultForm(var start: Int, var end: Int, resultName: String) extends ResultForm

  object MsaResultForm {

    implicit def rw: RW[MsaResultForm] = macroRW

  }

  case class ClustalResultForm(var start: Int, var end: Int, color: Boolean, resultName: String) extends ResultForm

  object ClustalResultForm {

    implicit def rw: RW[ClustalResultForm] = macroRW

  }

}
