package models.search

import javax.inject.{Inject, Named, Singleton}

import com.sksamuel.elastic4s._
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import modules.tools.FNV
import org.elasticsearch.common.unit.Fuzziness

import scala.concurrent.Future


@Singleton
class JobDAO @Inject()(cs: ClusterSetup,
                       elasticFactory: PlayElasticFactory,
                       @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {
  
  private[this] lazy val client = elasticFactory(cs)
  private val noHash = Set("mainID", "jobID")

  def generateHash(toolname: String, params: Map[String, String]): BigInt =  {

    FNV.hash64((scala.collection.immutable.TreeMap((params -- noHash).toArray:_*)
      .values ++ Iterable[String](toolname))
      .map(_.toByte).toArray)
  }

  def generateHash2(toolname: String, params: Map[String, String]): Int = {

    (scala.collection.immutable.TreeMap((params -- noHash).toArray:_*)
      .values ++ Iterable[String](toolname)).hashCode()
  }

  // Searches for a matching hash in the Hash DB
  def matchHash(hash : Any, dbName : Option[String], dbMtime : Option[String]): Future[RichSearchResponse] = {
    client.execute(
      search in "tkplay_dev"->"jobhashes" query {
          bool(
            must(
              termQuery("hash", hash),
              termQuery("dbname", dbName.getOrElse("none")),
              termQuery("dbmtime", dbMtime.getOrElse("1970-01-01T00:00:00Z"))
            )
          )
      }
    )
  }

  // Removes a Hash from ES
  def deleteJob(mainID : String): Future[BulkResult] = {
    client.execute {
      bulk(
        delete id mainID from "tkplay_dev" / "jobs",
        delete id mainID from "tkplay_dev" / "jobhashes"
        )
    }
  }

  // Checks if a jobID already exists
  def existsJobID(jobID : String): Future[RichSearchResponse] = {
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
  def getJobIDs(jobIDs : List[String]): Future[RichSearchResponse] = {
    client.execute{
      search in "tkplay_dev" -> "jobs" query {
        termsQuery("jobID", jobIDs : _*) // - termsQuery does not seem to work

        //termQuery("jobID", jobIDs.headOption.getOrElse("")) // Currently just looking for the first jobID
      }
    }
  }

  def jobIDtermSuggester(queryString : String): Future[RichSearchResponse] = { // this is a spelling correction mechanism
   client.execute {
      search in "tkplay_dev"->"jobs" suggestions {
        termSuggestion("jobID") field "jobID" text queryString mode SuggestMode.Always
      }
    }
  }


  def jobIDcompletionSuggester(queryString : String): Future[RichSearchResponse] = { // this is an auto-completion mechanism
    client.execute {
      search in "tkplay_dev"->"jobs" types "jobs" suggestions (
        //completionSuggestion("jobs-completer") field "jobID" text queryString size 10
        completionSuggestion field "jobID" text queryString size 10
      )
    }
  }


  def fuzzySearchJobID(queryString : String): Future[RichSearchResponse] = { // similarity search with Levensthein edit distance
    client.execute {
      search in "tkplay_dev"->"jobs" query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  }
}
