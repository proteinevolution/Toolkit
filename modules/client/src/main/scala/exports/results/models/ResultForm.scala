package exports.results.models

import upickle.default.{ macroRW, ReadWriter => RW }

sealed trait ResultForm {
  def start: Int
  def end: Int
  def copy(end: Int): ResultForm
}

object ResultForm {

  implicit def rw: RW[ResultForm] =
    RW.merge(ShowHitsForm.rw, MsaResultForm.rw, ClustalResultForm.rw)

  case class ShowHitsForm(
      start: Int,
      end: Int,
      wrapped: Boolean,
      isColor: Boolean
  ) extends ResultForm {
    override def copy(newEnd: Int): ShowHitsForm =
      new ShowHitsForm(start, newEnd, wrapped, isColor)
  }

  object ShowHitsForm {

    implicit def rw: RW[ShowHitsForm] = macroRW

  }

  case class MsaResultForm(start: Int, end: Int, resultName: String)
      extends ResultForm {
    override def copy(newEnd: Int): MsaResultForm =
      new MsaResultForm(start, newEnd, resultName)
  }

  object MsaResultForm {

    implicit def rw: RW[MsaResultForm] = macroRW

  }

  case class ClustalResultForm(
      start: Int,
      end: Int,
      color: Boolean,
      resultName: String
  ) extends ResultForm {
    override def copy(newEnd: Int): ClustalResultForm =
      new ClustalResultForm(start, newEnd, color, resultName)
  }

  object ClustalResultForm {

    implicit def rw: RW[ClustalResultForm] = macroRW

  }

}
