package de.proteinevolution.tools.services

import javax.inject.Inject

import cats.data.Kleisli
import de.proteinevolution.db.ResultFileAccessor

class KleisliProvider @Inject()(toolFinder: ToolNameGetService, resultFiles: ResultFileAccessor) {
  def resK  = Kleisli(resultFiles.getResults)
  def toolK = Kleisli(toolFinder.getTool)
}
