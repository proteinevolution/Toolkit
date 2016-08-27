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


  def matchHash(hash : String, dbName : Option[String], dbMtime : Option[String]) = {
    val resp = client.execute(
      search in "tkplay_dev"->"jobhashes" query {
          bool(
            must(
              termQuery("hash", hash),
              termQuery("dbname", dbName.get),
              termQuery("dbmtime", dbMtime.get)
            )
          )
      }
    )
    resp
  }


  def deleteJob(jobId : String) = {
    client.execute {
      bulk(
        delete id jobId from "tkplay_dev" / "jobs",
        delete id jobId from "tkplay_dev" / "jobhashes"
        )
    }
  }


}
