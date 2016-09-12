package models.search

import javax.inject.{Named, Inject}
import com.sksamuel.elastic4s.{IndexAndType, ElasticDsl}
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import org.elasticsearch.common.unit.Fuzziness

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


  def deleteJob(mainID : String) = {
    client.execute {
      bulk(
        delete id mainID from "tkplay_dev" / "jobs",
        delete id mainID from "tkplay_dev" / "jobhashes"
        )
    }
  }

  def findAutoCompleteJobID(queryString : String) = {
    client.execute {
      search in "tkplay_dev"->"jobs" suggestions {
        termSuggestion("jobID").field("jobID").text(queryString)
      }
    }
  }

  def fuzzySearchJobID(queryString : String) = {
    client.execute {
      search in "tkplay_dev"->"jobs" query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  }
}
