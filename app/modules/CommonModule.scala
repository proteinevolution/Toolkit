package modules

import models.database.{FrontendJob, Job, JobAnnotation, User}
import com.typesafe.config.{ConfigException, ConfigFactory}
import models.tools.ToolModel
import play.api.Logger
import play.api.libs.json.JsValue
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONArray, BSONDocument, BSONObjectID, BSONValue}

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 03.08.16.
  */
trait CommonModule extends ReactiveMongoComponents {


  private final def selectjobID(jobID: String) = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))
  private final def selectmainID(mainID: BSONObjectID) = BSONDocument("mainID" -> BSONDocument("$eq" -> mainID))

  /* Collections */
  protected def hashCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // jobCollection is now a value with the Index structure ensured
  protected val jobCollection : Future[BSONCollection] = {

      reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
        collection.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true))
        collection
      }
  }


  protected def frontendJobCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("frontendjobs"))
  protected def jobAnnotationCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobannotations"))
  // ResultfilesCollection
  protected def resultCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("results"))

  /* Accesssors */
  protected def addJob(job: Job) : Future[WriteResult] = jobCollection.flatMap(_.insert(job))

  protected def addFrontendJob(frontendJob: FrontendJob) : Future[WriteResult] = frontendJobCollection.flatMap(_.insert(frontendJob))

  //protected def addJobAnnotation(notes : JobAnnotation) : Future[WriteResult] = jobAnnotationCollection.flatMap(_.insert(JobAnnotation))

  protected def result2Job(jobID: String, key: String, result: JsValue): Unit = {
    val bson = reactivemongo.play.json.BSONFormats.toBSON(result).get
    modifyResult(BSONDocument("jobID" -> jobID), BSONDocument("$set" -> BSONDocument(key -> bson)))
  }

  // Able to fetch one field for the respective result
  protected def getResult(jobID: String, key: String): Future[Option[JsValue]] = {
    val projection = BSONDocument(key -> 1)
    val selector = BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))
    resultCollection.map(_.find(selector, projection).cursor[BSONDocument]()).flatMap(_.headOption).map {
      case Some(bsonDoc) => Some(reactivemongo.play.json.BSONFormats.toJSON(bsonDoc))
      case None => None
    }
  }


  protected def findJob(selector : BSONDocument) : Future[Option[Job]] = jobCollection.flatMap(_.find(selector).one[Job])

  protected def findFrontendJob(selector : BSONDocument) : Future[Option[FrontendJob]] = frontendJobCollection.flatMap(_.find(selector).one[FrontendJob])

  protected def findJobAnnotation(selector : BSONDocument) : Future[Option[JobAnnotation]] = jobAnnotationCollection.flatMap(_.find(selector).one[JobAnnotation])

  protected def findJobs(selector : BSONDocument) : Future[scala.List[Job]] = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }


  protected def selectJob(jobID: String): Future[Option[Job]] = {

    jobCollection
      .map(_.find(BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))).cursor[Job]())
      .flatMap(_.headOption)
  }

  // this is not in use anymore and is being replaced by an elasticsearch query
  protected def selectJobs(jobIDs: Traversable[String]): Future[Set[Job]] = {
    jobCollection
        .map(_.find(BSONDocument("jobID" -> BSONDocument("$in" -> BSONArray(jobIDs)))).cursor[Job]())
        .flatMap(_.collect[Set](-1, Cursor.FailOnError[Set[Job]]()))
  }


  /**
    * Updates Job in the database with, if a job with the same jobID is already present, otherwise creates the Job
    *
    * @param job
    * @return
    */
  protected def upsertJob(job: Job) : Future[Option[Job]] =  {

    jobCollection
      .flatMap(_.findAndUpdate(selectjobID(job.jobID), update = job, upsert = true).map(_.result[Job]))
  }

  protected def upsertAnnotation(notes: JobAnnotation) : Future[Option[JobAnnotation]] =  {

    jobAnnotationCollection
      .flatMap(_.findAndUpdate(selectjobID(notes.jobID), update = notes, upsert = true).map(_.result[JobAnnotation]))
  }

  // Modifies result in database
  protected def modifyResult(selector: BSONDocument, modifier: BSONDocument) = {
    resultCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true, upsert = true).map(_.result))
  }


  // Modifies Job in the database
  protected def modifyJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def modifyFrontendJob(selector : BSONDocument, modifier : BSONDocument) = {
    frontendJobCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def modifyJobAnnotation(selector : BSONDocument, modifier : BSONDocument) : Future[Option[Job]] = {
    jobAnnotationCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
  }

  protected def updateJob(selector : BSONDocument, modifier : BSONDocument) = {
    jobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  protected def updateFrontendJob(selector : BSONDocument, modifier : BSONDocument) = {
    frontendJobCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  protected def updateJobAnnotation(selector : BSONDocument, modifier : BSONDocument) : Future[UpdateWriteResult] = {
    jobAnnotationCollection.flatMap(_.update(selector, modifier, multi = true))
  }


  protected def removeJob(selector : BSONDocument) = jobCollection.flatMap(_.remove(selector))


  // User DB access
  protected def userCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  protected def addUser(user : User) = userCollection.flatMap(_.insert(user))

  protected def findUser(selector : BSONDocument) = userCollection.flatMap(_.find(selector).one[User])

  protected def modifyUser(selector : BSONDocument, modifier : BSONDocument) = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }
  protected def removeUser(selector : BSONDocument) = userCollection.flatMap(_.remove(selector))

  // tool version lookup from the config

  protected def toolVersion(name: String) : Option[String] = {

    try {
      Some(ConfigFactory.load().getConfig("Tools").getString(s"$name.version"))
    }
    catch {
      case _ => None
    }
  }

  protected def toolMap : Map[String, ToolModel] = ToolModel.values map (_.toolNameShort) zip ToolModel.values toMap
}
