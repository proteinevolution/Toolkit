package models.search

import javax.inject.{Named, Inject}
import com.sksamuel.elastic4s.{SuggestMode, IndexAndType, ElasticDsl}
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import jdk.internal.org.objectweb.asm.tree.analysis.Analyzer
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

  // Simple multiple jobID search
  def getJobIDs(jobIDs : List[String]) = {
    client.execute{
      search in "tkplay_dev" -> "jobs" query {
        //termsQuery("jobID", jobIDs) // - termsQuery does not seem to work
        termQuery("jobID", jobIDs.headOption.getOrElse("")) // Currently just looking for the first jobID
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


  def jobIDcompletionSuggester(queryString : String) = { // this is an auto-completion mechanism
    client.execute {
      search in "tkplay_dev"->"jobs" types "jobs" suggestions (
        //completionSuggestion("jobs-completer") field "jobID" text queryString size 10
        completionSuggestion field "jobID" text queryString size 10
      )
    }
  }


  def fuzzySearchJobID(queryString : String) = { // similarity search with Levensthein edit distance
    client.execute {
      search in "tkplay_dev"->"jobs" query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  }
}
