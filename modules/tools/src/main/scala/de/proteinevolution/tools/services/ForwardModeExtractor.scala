package de.proteinevolution.tools.services

import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird.PathBindableExtractor
import de.proteinevolution.tools.models.ForwardMode

trait ForwardModeExtractor {

  implicit object forwardModeBindable
      extends Parsing[ForwardMode](
        _.trim match {
          case "alnEval"  => ForwardMode("alnEval")
          case "evalFull" => ForwardMode("evalFull")
          case "aln"      => ForwardMode("aln")
          case "full"     => ForwardMode("full")
        },
        _.toString,
        (_: String, _: Exception) => "string not a valid forwarding mode"
      )

  val forwardModeExtractor: PathBindableExtractor[ForwardMode] =
    new PathBindableExtractor[ForwardMode]

}
