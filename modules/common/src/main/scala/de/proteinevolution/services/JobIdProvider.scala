package de.proteinevolution.services

import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.stream.Materializer
import de.proteinevolution.db.MongoStore
import play.api.inject.ApplicationLifecycle

import scala.concurrent.ExecutionContext

@Singleton
class JobIdProvider @Inject()(applicationLifecycle: ApplicationLifecycle, mongoStore: MongoStore)(
    implicit ec: ExecutionContext,
    mat: Materializer
) {

  implicit val system = ActorSystem("JobIdProvider")



}
