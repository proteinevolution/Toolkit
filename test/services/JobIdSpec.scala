package services

import de.proteinevolution.db.MongoStore
import de.proteinevolution.services.JobIdProvider
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import com.softwaremill.macwire._
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global

class JobIdSpec extends FlatSpec with MockitoSugar {

  lazy val mongoStore: MongoStore             = wire[MongoStore]
  lazy val reactiveMongoApi: ReactiveMongoApi = mock[ReactiveMongoApi]
  lazy val jobIdProvider                      = new JobIdProvider(mongoStore)

  "A jobId" should "be unique" in {

    // TODO fixtures for mongodb

  }

}
