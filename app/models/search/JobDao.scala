package models.search




import javax.inject.{Named, Inject}

import models.database.Job

import scala.concurrent.{ExecutionContext, Future}

import com.sksamuel.elastic4s.{IndexAndType, ElasticDsl}

import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}

class JobDAO @Inject()(cs: ClusterSetup, elasticFactory: PlayElasticFactory, @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {
  
  private[this] lazy val client = elasticFactory(cs)

  def getJobById(jobId: String)(implicit ec: ExecutionContext): Future[Option[Job]] = client execute {
    get id jobId from indexAndType
  } map (_.as[Job])

  // the above .as[Book] conversion is available as an extension method
  // provided by PlayElasticJsonSupport

  def indexJobs(jobId: String, job: Job) = client execute {
    index into indexAndType source job id jobId
  }
  // original elastic4s .source(doc) expects a DocumentSource or T : Indexable.
  // PlayElasticJsonSupport provides Indexable[T] for any T with Json.Writes[T] available.

  def bulkIndex(jobs: Iterable[Job]) = client execute {
    bulk {
      jobs map (job => index into indexAndType source job)
    }
  }

  def searchByQueryString(q: String)(implicit ec: ExecutionContext) = client execute {
    search in indexAndType query queryStringQuery(q)
  } map (_.as[Job])


  // the .as[T] conversion is available in elastic4s for any T with HitAs[T] instance available.
  // PlayElasticJsonSupport automatically derives HitAs[T] based on Json.Reads[T].


}
