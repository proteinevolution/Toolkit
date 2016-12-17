package models.search

import javax.inject.{Inject, Named, Singleton}

import com.sksamuel.elastic4s._
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import modules.tools.FNV
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.common.unit.Fuzziness
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future


@Singleton
class JobDAO @Inject()(cs: ClusterSetup,
                       elasticFactory: PlayElasticFactory,
                       @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport {
  
  private[this] lazy val client = elasticFactory(cs)
  private val noHash = Set("mainID", "jobID")

  private val Index = "tkplay_dev"
  private val jobIndex = Index / "jobs"
  private val jobHashIndex = Index / "jobhashes"

  def generateHash(toolname: String, params: Map[String, String]): BigInt =  {

    FNV.hash64((scala.collection.immutable.TreeMap((params -- noHash).toArray:_*)
      .values ++ Iterable[String](toolname))
      .toString().getBytes)

  }


  // Searches for a matching hash in the Hash DB
  def matchHash(hash : String, dbName : Option[String], dbMtime : Option[String]): Future[RichSearchResponse] = {
    client.execute(
      search in jobHashIndex query {
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
        delete id mainID from jobIndex,
        delete id mainID from jobHashIndex
        )
    }
  }

  // Checks if a jobID already exists
  def existsJobID(jobID : String): Future[RichSearchResponse] = {
    client.execute{
      search in jobIndex query {
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
      search in jobIndex query {
        termsQuery("jobID", jobIDs : _*) // - termsQuery does not seem to work
      }
    }
  }

  def jobIDtermSuggester(queryString : String): Future[RichSearchResponse] = { // this is a spelling correction mechanism, don't use this for autocompletion
   client.execute {
      search in jobIndex suggestions {
        termSuggestion("jobID") field "jobID" text queryString mode SuggestMode.Always
      }
    }
  }


  // only use this for setting completion type for the jobID field

  def preMap : CreateIndexResponse = {

    client.execute {
      createIndex("tkplay_dev").mappings(
        mapping("jobs").fields(
          completionField("jobID")
        )
      )
    }.await
  }


  def jobIDcompletionSuggester(queryString : String): Future[RichSearchResponse] = {
    client.execute {
      search(jobIndex).suggestions {
        completionSuggestion("a").field("jobID").text(queryString).size(10)
      }
    }
  }


  def fuzzySearchJobID(queryString : String): Future[RichSearchResponse] = { // similarity search with Levensthein edit distance
    client.execute {
      search in jobIndex query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  }


  def jobsWithTool(toolName : String, userID : BSONObjectID) : Future[RichSearchResponse] = {
    client.execute {
      search in jobIndex query {
        bool(
          should(
            termQuery("tool", toolName),
            termQuery("ownerID", userID)
          )
        )
      }
    }
  }
}
