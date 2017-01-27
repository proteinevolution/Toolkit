package modules

import models.database.{FrontendJob, Job, JobAnnotation, User}
import models.tools.ToolModel
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

  /* Collections */
  protected lazy val hashCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))

  // jobCollection is now a value with the Index structure ensured
  protected lazy val jobCollection : Future[BSONCollection] = {

      reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs")).map { collection =>
        collection.indexesManager.ensure(Index(Seq("jobID" -> IndexType.Text), background = true, unique = true))
        collection
      }
  }


  protected lazy val frontendJobCollection : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("frontendjobs"))
  protected lazy val jobAnnotationCollection :Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobannotations"))
  // ResultfilesCollection
  protected lazy val resultCollection: Future[BSONCollection] = reactiveMongoApi.database.map(_.collection[BSONCollection]("results"))

  /* Accesssors */

  protected def addFrontendJob(frontendJob: FrontendJob) : Future[WriteResult] = frontendJobCollection.flatMap(_.insert(frontendJob))


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

  protected def findJobAnnotation(selector : BSONDocument) : Future[Option[JobAnnotation]] = jobAnnotationCollection.flatMap(_.find(selector).one[JobAnnotation])

  protected def findJobs(selector : BSONDocument) : Future[scala.List[Job]] = {
    jobCollection.map(_.find(selector).cursor[Job]()).flatMap(_.collect[List](-1, Cursor.FailOnError[List[Job]]()))
  }


  protected def selectJob(jobID: String): Future[Option[Job]] = {

    jobCollection
      .map(_.find(BSONDocument("jobID" -> BSONDocument("$eq" -> jobID))).cursor[Job]())
      .flatMap(_.headOption)
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

    jobAnnotationCollection.flatMap(_.findAndUpdate(selectjobID(notes.jobID), update = notes, upsert = true).map(_.result[JobAnnotation]))
  }

  protected def modifyAnnotation(selector : BSONDocument, modifier : BSONDocument) = {
    jobAnnotationCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[Job]))
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




  // User DB access
  protected def userCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  protected def addUser(user : User) = userCollection.flatMap(_.insert(user))

  protected def findUser(selector : BSONDocument) = userCollection.flatMap(_.find(selector).one[User])

  protected def modifyUser(selector : BSONDocument, modifier : BSONDocument) = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }
  protected def removeUser(selector : BSONDocument) = userCollection.flatMap(_.remove(selector))

  // tool version lookup from the config


  protected lazy val toolMap : Map[String, ToolModel] = ToolModel.values map (_.toolNameShort) zip ToolModel.values toMap
}
