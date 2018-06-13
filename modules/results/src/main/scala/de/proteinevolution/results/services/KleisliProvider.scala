package de.proteinevolution.results.services

import cats.data.Kleisli
import de.proteinevolution.db.ResultFileAccessor
import javax.inject.Inject

class KleisliProvider @Inject()(toolFinder: ToolNameGetService, resultFiles: ResultFileAccessor) {
  def resK  = Kleisli(resultFiles.getResults)
  def toolK = Kleisli(toolFinder.getTool)
}
