package de.proteinevolution.backend

import com.google.inject.AbstractModule
import de.proteinevolution.backend.actors.DatabaseMonitor
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DatabaseMonitor]("databaseMonitor")
  }

}
