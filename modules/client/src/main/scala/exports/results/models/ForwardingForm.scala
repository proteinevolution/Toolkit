package exports.results.models

import upickle.default.{ macroRW, ReadWriter => RW }

sealed trait ForwardingForm

object ForwardingForm {

  implicit def rw: RW[ForwardingForm] =
    RW.merge(ForwardingFormNormal.rw, ForwardingFormAln.rw)

  case class ForwardingFormNormal(
      fileName: String,
      evalue: String,
      checkboxes: Array[Int]
  ) extends ForwardingForm

  object ForwardingFormNormal {

    implicit def rw: RW[ForwardingFormNormal] = macroRW

  }

  case class ForwardingFormAln(resultName: String, checkboxes: Array[Int])
      extends ForwardingForm

  object ForwardingFormAln {

    implicit def rw: RW[ForwardingFormAln] = macroRW

  }

}
