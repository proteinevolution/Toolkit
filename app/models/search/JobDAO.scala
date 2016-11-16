package models.search

import javax.inject.{Named, Inject}
import com.sksamuel.elastic4s.{SuggestMode, IndexAndType, ElasticDsl}
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import org.elasticsearch.common.unit.Fuzziness

class JobDAO @Inject()(cs: ClusterSetup, elasticFactory: PlayElasticFactory, @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {
  
  private[this] lazy val client = elasticFactory(cs)


  // Searches for a matching hash in the Hash DB
  def matchHash(hash : String, dbName : Option[String], dbMtime : Option[String]) = {
    client.execute(
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
  }

  // Removes a Hash from ES
  def deleteJob(mainID : String) = {
    client.execute {
      bulk(
        delete id mainID from "tkplay_dev" / "jobs",
        delete id mainID from "tkplay_dev" / "jobhashes"
        )
    }
  }

  // Checks if a jobID already exists
  def existsJobID(jobID : String) = {
    client.execute{
      search in "tkplay_dev"->"jobs" query {
        bool(
          must(
            termQuery("jobID", jobID)
          )
        )
      }
    }
  }


  def jobIDtermSuggester(queryString : String) = { // this is a spelling correction mechanism
   client.execute {
      search in "tkplay_dev"->"jobs" suggestions {
        termSuggestion("jobID") field "jobID" text queryString mode SuggestMode.Always
      }
    }

  }



  def jobIDcompletionSuggester(queryString : String) = { // this is a auto-completion mechanism
    client.execute {
      search in "tkplay_dev"->"jobs" suggestions {
        completionSuggestion("job-completion") field "jobID" text queryString size 10
      }
    }
  }


  // tries to find a matching Job with the Job ID
  def fuzzySearchJobID(queryString : String) = {
    client.execute {
      search in "tkplay_dev"->"jobs" query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  }
}
