package de.proteinevolution.results.results
import de.proteinevolution.results.results.General.SingleSeq
import de.proteinevolution.results.results.Quick2DResult._
import io.circe.{ Decoder, HCursor }

case class Quick2DResult(
    jobID: String,
    query: SingleSeq,
    psipred: Psipred,
    pipred: Pipred,
    marcoil: Marcoil,
    coils: Coils,
    pcoils: Pcoils,
    tmhmm: Tmhmm,
    phobius: Phobius,
    polyphobius: Polyphobius,
    spider2: Spider2,
    spotd: Spotd,
    iupred: Iupred,
    disopred3: Disopred3,
    signal: Signal,
    psspred: Psspred,
    deepcnf: Deepcnf
)

object Quick2DResult {

  implicit val quick2dDecoder: Decoder[Quick2DResult] = (c: HCursor) => {
      for {

      } yield {

      }
  }


  case class Psipred(name: String, seq: String, conf: String)
  case class Pipred(name: String, seq: String, conf: String)
  case class Marcoil(name: String, seq: String)
  case class Coils(name: String, seq: String)
  case class Pcoils(name: String, seq: String)
  case class Tmhmm(name: String, seq: String)
  case class Phobius(name: String, seq: String)
  case class Polyphobius(name: String, seq: String)
  case class Spider2(name: String, seq: String)
  case class Spotd(name: String, seq: String)
  case class Iupred(name: String, seq: String)
  case class Disopred3(name: String, seq: String)
  case class Signal(name: String, seq: String)
  case class Psspred(name: String, seq: String)
  case class Deepcnf(name: String, seq: String)

}
