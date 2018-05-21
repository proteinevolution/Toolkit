package de.proteinevolution.tools.services

import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird.PathBindableExtractor
import de.proteinevolution.tools.models.ForwardMode

trait ForwardModeExtractor {

  implicit object forwardModeBindable
      extends Parsing[ForwardMode](
        _.trim match {
          case validString if ("alnEval" :: "evalFull" :: "aln" :: "full" :: Nil).contains(validString) =>
            ForwardMode(validString)
          case _ => throw new IllegalArgumentException
        },
        _.toString,
        (_: String, _: Exception) => "string not a valid forwarding mode"
      )

  val forwardModeExtractor: PathBindableExtractor[ForwardMode] =
    new PathBindableExtractor[ForwardMode]

}
