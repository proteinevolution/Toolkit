package models.search

import javax.inject.{Inject, Named, Singleton}

import com.sksamuel.elastic4s._
import com.evojam.play.elastic4s.configuration.ClusterSetup
import com.evojam.play.elastic4s.{PlayElasticFactory, PlayElasticJsonSupport}
import com.sksamuel.elastic4s.analyzers.{StandardAnalyzer, WhitespaceAnalyzer}
import com.typesafe.config.ConfigFactory
import models.tools.ToolFactory
import modules.tel.TELConstants
import modules.tools.FNV
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.common.unit.Fuzziness
import reactivemongo.bson.BSONObjectID

import scala.util.hashing.MurmurHash3
import scala.concurrent.Future


@Singleton
final class JobDAO @Inject()(cs: ClusterSetup,
                       elasticFactory: PlayElasticFactory,
                       toolFactory: ToolFactory,
                       @Named("jobs") indexAndType: IndexAndType)
  extends ElasticDsl with PlayElasticJsonSupport with TELConstants {
  
  private[this] lazy val client = elasticFactory(cs)
  private val noHash = Set("mainID", "jobID")

  private val Index = "tkplay_dev"
  private val jobIndex = Index / "jobs"
  private val jobHashIndex = Index / "jobhashes"


  //private def toolNameLong(name : String) : String = toolFactory.values.get(name).get.toolNameLong

  /**
    * generates Param hash for matching already existing jobs
    * @param params
    * @return
    */

  def generateHash(params: Map[String, String]): BigInt =  {

    FNV.hash64(params.toString.getBytes())

  }


  /**
    * hashes the runscripts which is used for a job
    * @param toolname
    * @return
    */

  def generateRSHash(toolname: String) : String = {

    val runscript = s"$runscriptPath$toolname.sh"
    val content = scala.io.Source.fromFile(runscript).getLines().mkString

    MurmurHash3.stringHash(content,0).toString

  }

  /**
    * hashes the tool version and version of helper scripts specified in the tools.conf config
    * @param name
    * @return
    */
  def generateToolHash(name: String) : String = {

    try {
      MurmurHash3.stringHash(ConfigFactory.load().getConfig(s"Tools.$name").toString,0).toString
    }
    catch {
      case _ : Throwable => "No matching hash value found"
    }

  }


  // Searches for a matching hash in the Hash DB
  def matchHash(hash : String, rsHash: String, dbName : Option[String], dbMtime : Option[String], toolname : String, toolHash: String): Future[RichSearchResponse] = {
    client.execute(
      search in jobHashIndex query {
          bool(
            must(
              matchQuery("hash", hash).analyzer(StandardAnalyzer),
              matchQuery("dbname", dbName.getOrElse("none")).analyzer(StandardAnalyzer),
              termQuery("dbmtime", dbMtime.getOrElse("1970-01-01T00:00:00Z")),
              matchQuery("toolname", toolname).analyzer(StandardAnalyzer),
              matchQuery("rshash", rsHash).analyzer(StandardAnalyzer),
              matchQuery("toolhash", toolHash).analyzer(StandardAnalyzer)
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

  // Checks if a mainID exists
  def existsMainID(mainID : String): Future[RichSearchResponse] = {
    client.execute{
      search in jobIndex query {
        bool(
          must(
            termQuery("_id", mainID)
          )
        )
      }
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



  def multiExistsJobID(set : Traversable[String]) : Future[RichSearchResponse] = {
    client.execute {
        search in jobIndex query termsQuery("jobID", set.toSeq: _* ) // opt: "limit 100"
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
