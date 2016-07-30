package modules

import actors.JobDatabaseActor
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import akka.actor.Actor
import com.google.inject.name.Names

/**
  * Created by lzimmermann on 29.04.16.
  */
class DatabaseModule extends AbstractModule with ScalaModule {
  override def configure() {
    // note that we DO NOT define the actor in scope singleton, and we MUST name it
    bind[Actor].annotatedWith(Names.named(JobDatabaseActor.name)).to[JobDatabaseActor]
  }
}