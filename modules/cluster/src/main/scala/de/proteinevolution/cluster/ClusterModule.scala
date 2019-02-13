package de.proteinevolution.cluster

import play.api.inject.{ Binding, Module }
import play.api.{ Configuration, Environment }

class ClusterModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[ClusterSource].toSelf.eagerly()
  )

}
