package models.search

import javax.inject.{ Inject, Named, Singleton }

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.analyzers.{ StandardAnalyzer, WhitespaceAnalyzer }
import com.sksamuel.elastic4s.ElasticDsl._
import com.typesafe.config.ConfigFactory
import models.database.jobs.JobHash
import models.tools.ToolFactory
import modules.RunscriptPathProvider
import modules.tel.TELConstants
import modules.tools.FNV
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.common.unit.Fuzziness
import play.libs.Json
import reactivemongo.bson.BSONObjectID

import scala.util.hashing.MurmurHash3
import scala.concurrent.Future

@Singleton
final class JobDAO @Inject()(toolFactory: ToolFactory, runscriptPathProvider: RunscriptPathProvider)
    extends ElasticDsl
    with TELConstants {

  private val noHash       = Set("mainID", "jobID")
  private val client       = ElasticClient.transport(ElasticsearchClientUri(ConfigFactory.load().getString(s"elastic4s.hostname"), 9300))
  private val Index        = ConfigFactory.load().getString(s"elastic4s.indexAndTypes.jobs.index")
  private val jobIndex     = Index / "jobs"
  private val jobHashIndex = Index / "jobhashes"

  //private def toolNameLong(name : String) : String = toolFactory.values.get(name).get.toolNameLong

  /**
    * generates Param hash for matching already existing jobs
    *
    * @param params
    * @return
    */
  def generateHash(params: Map[String, String]): BigInt = {

    FNV.hash64(params.toString.getBytes())

  }

  /**
    * hashes the runscripts which is used for a job
    *
    * @param toolname
    * @return
    */
  def generateRSHash(toolname: String): String = {
    val runscript = runscriptPathProvider.get() + s"$toolname.sh"
    val source    = scala.io.Source.fromFile(runscript)
    val content   = try { source.getLines().mkString } finally { source.close() }

    MurmurHash3.stringHash(content, 0).toString

  }

  /**
    * hashes the tool version and version of helper scripts specified in the tools.conf config
    *
    * @param name
    * @return
    */
  def generateToolHash(name: String): String = {

    try {
      MurmurHash3.stringHash(ConfigFactory.load().getConfig(s"Tools.$name").toString, 0).toString
    } catch {
      case _: Throwable => "No matching hash value found"
    }

  }

  // Searches for a matching hash in the Hash DB
  def matchHash(hash: String,
                rsHash: String,
                dbName: Option[String],
                dbMtime: Option[String],
                toolname: String,
                toolHash: String) = {
    client.execute(
      search in jobHashIndex query {
        bool(
          must(
            matchQuery(JobHash.INPUTHASH, hash).analyzer(StandardAnalyzer),
            matchQuery(JobHash.DBNAME, dbName.getOrElse("none")).analyzer(StandardAnalyzer),
            termQuery(JobHash.DBMTIME, dbMtime.getOrElse("1970-01-01T00:00:00Z")),
            matchQuery(JobHash.TOOLNAME, toolname).analyzer(StandardAnalyzer),
            matchQuery(JobHash.RUNSCRIPTHASH, rsHash).analyzer(StandardAnalyzer),
            matchQuery(JobHash.TOOLHASH, toolHash).analyzer(StandardAnalyzer)
          )
        )
      }
    )
  }

  // Searches for a matching hash in the Hash DB
  def matchHash(jobHash: JobHash) = {
    client.execute(
      search in jobHashIndex query {
        bool(
          must(
            matchQuery(JobHash.INPUTHASH, jobHash.inputHash).analyzer(StandardAnalyzer),
            matchQuery(JobHash.DBNAME, jobHash.dbName.getOrElse("none")).analyzer(StandardAnalyzer),
            termQuery(JobHash.DBMTIME, jobHash.dbMtime.getOrElse("1970-01-01T00:00:00Z")),
            matchQuery(JobHash.TOOLNAME, jobHash.toolName).analyzer(StandardAnalyzer),
            matchQuery(JobHash.RUNSCRIPTHASH, jobHash.runscriptHash).analyzer(StandardAnalyzer),
            matchQuery(JobHash.TOOLHASH, jobHash.toolHash).analyzer(StandardAnalyzer)
          )
        )
      }
    )
  }

  // Removes a Hash from ES
  def deleteJob(mainID: String) = {
    client.execute {
      bulk(
        delete id mainID from jobIndex,
        delete id mainID from jobHashIndex
      )
    }
  }

  // Checks if a mainID exists
  def existsMainID(mainID: String) = {
    client.execute {
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
  def existsJobID(jobID: String) = {
    client.execute {
      search in jobIndex query {
        bool(
          must(
            termQuery("jobID", jobID)
          )
        )
      }
    }
  }

  def jobIDtermSuggester(queryString: String) = { // this is a spelling correction mechanism, don't use this for autocompletion
    client.execute {
      search in jobIndex suggestions {
        termSuggestion("jobID") field "jobID" text queryString mode SuggestMode.Always
      }
    }
  }

  // only use this for setting completion type for the jobID field

  def preMap: Future[CreateIndexResponse] = {
    client.execute {
      createIndex("tkplay_dev").mappings(
        mapping("jobs").fields(
          completionField("jobID")
        )
      )
    }
  }

  def jobIDcompletionSuggester(queryString: String) = {
    val suggestionBuild = search in jobIndex suggestions {
      completionSuggestion("jobIDfield").field("jobID").text(queryString).size(10)
    }
    println(suggestionBuild)
    client.execute {
      suggestionBuild
    }
  }

  /*def fuzzySearchJobID(queryString: String) = { // similarity search with Levensthein edit distance
    client.execute {
      search in jobIndex query {
        fuzzyQuery("jobID", queryString).fuzziness(Fuzziness.AUTO).prefixLength(4).maxExpansions(10)
      }
    }
  } */

  def jobsWithTool(toolName: String, userID: BSONObjectID) = {
    val queryBuild = search in jobIndex query {
      bool(
        should(
          termQuery("tool", toolName),
          termQuery("ownerID", userID)
        )
      )
    }
    println(queryBuild)
    client.execute {
      queryBuild
    }
  }
}
