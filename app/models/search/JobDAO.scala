package models.search


import javax.inject.{Named, Inject}

import models.database.Job
import play.Logger
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import com.sksamuel.elastic4s.{IndexAndType, ElasticDsl}

import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}

class JobDAO @Inject()(cs: ClusterSetup, elasticFactory: PlayElasticFactory, @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {
  
  private[this] lazy val client = elasticFactory(cs)

  // TODO mainID is not returned by the query function, so a Job can not be read this way. See if there is a get id with id function
  def getJobById(jobId: String)(implicit ec: ExecutionContext): Future[Option[Job]] = client execute {
    get id jobId from indexAndType
  } map (_.as[Job])

  def getJobByIdAsJSObject(jobId: String)(implicit ec: ExecutionContext): Future[Option[JsObject]] = client execute {
    get id jobId from indexAndType
  } map (_.as[JsObject])

  def indexJobs(jobId: String, job: Job) = client execute {
    index into indexAndType source job id jobId
  }


  //inputHash : String, dbName : Option[String], dbMtime : Option[String]
  def matchHash =  client.execute { search in "tkplay_dev" -> "jobhashes" query s"hash:675446171527794326"}

  def getHash(hash : String, dbName : Option[String], dbMtime : Option[String]) = {
    val resp = client.execute(
      search in "tkplay_dev"->"jobhashes" query s"hash:${hash}"
    )
    resp
  }


  def bulkIndex(jobs: Iterable[Job]) = client execute {
    bulk {
      jobs map (job => index into indexAndType source job)
    }
  }

  def searchByQueryString(q: String)(implicit ec: ExecutionContext) = client execute {
    search in indexAndType query queryStringQuery(q)
  } map (_.as[Job])
  

}
