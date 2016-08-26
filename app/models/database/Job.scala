package models.database

import JobState.JobState
import org.elasticsearch.common.joda.Joda
import org.joda.time.DateTime

import reactivemongo.bson._
import play.api.libs.json._
import reactivemongo.play.json._

/** ?
  *
  * @param mainID       ID of the Job in the database
  * @param jobType
  * @param parentID     ID of the parent Job
  * @param jobID        User Visible Job ID
  * @param userID       Logged in Users will have their ID here
  * @param status       status of the Job
  * @param tool         name of the tool used for the Job
  * @param statID
  * @param dateCreated  date on which the Job was created
  * @param dateUpdated  date on which the Job was updated last
  * @param dateViewed   date on which the Job was viewed last
  *
  * Maps MySQL schema with some fields renamed (e.g. type is a reserved word in Scala)
  * +------------+------------------+------+-----+-------------------+-----------------------------+
    | Field      | Type             | Null | Key | Default           | Extra                       |
    +------------+------------------+------+-----+-------------------+-----------------------------+
    | main_id    | int(10) unsigned | NO   | PRI | NULL              | auto_increment              |
    | type       | varchar(50)      | YES  |     | NULL              |                             |
    | parent_id  | int(11)          | YES  |     | NULL              |                             |
    | job_id     | varchar(100)     | YES  |     | NULL              |                             |
    | user_id    | int(11)          | YES  |     | NULL              |                             |
    | status     | char(1)          | YES  |     | NULL              |                             |
    | tool       | varchar(100)     | YES  |     | NULL              |                             |
    | stat_id    | int(11)          | YES  |     | NULL              |                             |
    | created_on | timestamp        | NO   |     | CURRENT_TIMESTAMP |                             |
    | updated_on | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    | viewed_on  | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    +------------+------------------+------+-----+-------------------+-----------------------------+
  *
  *
  */


case class Job(mainID      : BSONObjectID,                // ID of the Job in the System
               jobType     : String,                      // Type of job
               parentID    : Option[BSONObjectID] = None, // ID of the Parent Job
               jobID       : String,                      // User visible ID of the Job
               userID      : BSONObjectID,                // User to whom the Job belongs
               status      : JobState,                    // Status of the Job
               tool        : String,                      // Tool used for this Job
               statID      : String,                      //
               watchList   : Option[List[BSONObjectID]] = None, // List of the users who watch this job, None if not public
               commentList : Option[List[BSONObjectID]] = None, // List of comments for the Job
               dateCreated : Option[DateTime],            // Creation time of the Job
               dateUpdated : Option[DateTime],            // Last Updated on
               dateViewed  : Option[DateTime]) {          // Last Viewed on

  /**
    * Returns the output file paths for the results
    *
    * @return
    */
  def resultFiles () = {
    tool match {
    //  The tool anlviz just returns the BioJS MSA Viewer page
    case "alnviz" =>
      Map("BioJS" -> s"/files/${mainID.stringify}/result")

    // For T-Coffee, we provide a simple alignment visualiation and the BioJS View
    case "tcoffee" =>
      Map("Simple" -> s"/files/${mainID.stringify}/sequences.clustalw_aln",
          "BioJS"  -> s"/files/${mainID.stringify}/sequences.clustalw_aln")

    case "reformatb" =>
      Map("Simple" -> s"/files/${mainID.stringify}/sequences.clustalw_aln",
          "BioJS"  -> s"/files/${mainID.stringify}/sequences.clustalw_aln")

    case "psiblast" =>
      Map("Results" -> s"/files/${mainID.stringify}/out.psiblastp",
          "BioJS"   -> s"/files/${mainID.stringify}/sequences.clustalw_aln",
          "Evalue"  -> s"/files/${mainID.stringify}/evalues.dat")

    // Hmmer just provides a simple file viewer.
    case "hmmer3" =>
      Map("domtbl"    -> s"/files/${mainID.stringify}/domtbl",
          "outfile"   -> s"/files/${mainID.stringify}/outfile",
          "multi_sto" -> s"/files/${mainID.stringify}/outfile_multi_sto",
          "table"     -> s"/files/${mainID.stringify}/tbl")
    }
  }
}




object Job {
  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val JOBTYPE       = "jobType"       //              Type of the Job
  val PARENTID      = "parentID"      //              ID of the parent job
  val JOBID         = "jobID"         //              ID for the job
  val USERID        = "userID"        //              ID of the job owner
  val STATUS        = "status"        //              Status of the job field
  val TOOL          = "tool"          //              name of the tool field
  val STATID        = "statID"        //              ID of the stats for this Job
  val WATCHLIST     = "watchList"     //              optional list of watching users references
  val COMMENTLIST   = "commentList"   //              comment list references
  val DATECREATED   = "dateCreated"   //              created on field
  val DATEUPDATED   = "dateUpdated"   //              changed on field
  val DATEVIEWED    = "dateViewed"    //              last view on field

  //implicit val format: Format[Job] = Json.format[Job]

  implicit object JsonReader extends Reads[Job] {
    override def reads(json: JsValue): JsResult[Job] = json match {
      case obj: JsObject => try {
        /*
        val mainID  = BSONObjectID.parse((obj \ IDDB).as[String]).get
        val jobType = (obj \ JOBTYPE).as[String]
        val parentID = BSONObjectID.parse((obj \ PARENTID).as[String]).toOption
        val jobID   = (obj \ JOBID).as[String]
        val userID  = BSONObjectID.parse((obj \ USERID).as[String]).get
        val status  = (obj \ STATUS).as[JobState]
        val tool    = (obj \ TOOL).as[String]
        val statID  = (obj \ STATID).as[String]
        val watchList = (obj \ WATCHLIST).asOpt[List[String]].map(_.map(string => BSONObjectID.parse(string).get))
        val commentList = (obj \ COMMENTLIST).asOpt[List[String]].map(_.map(string => BSONObjectID.parse(string).get))
        val dateCreated = (obj \ DATECREATED).asOpt[String].map(time => DateTime.parse(time))
        val dateUpdated = (obj \ DATEUPDATED).asOpt[String].map(time => DateTime.parse(time))
        val dateViewed  = (obj \ DATEVIEWED).asOpt[String].map(time => DateTime.parse(time))
        JsSuccess(Job(
          mainID      = mainID,
          jobType     = jobType,
          parentID    = parentID,
          jobID       = jobID,
          userID      = userID,
          status      = status,
          tool        = tool,
          statID      = statID,
          watchList   = watchList,
          commentList = commentList,
          dateCreated = dateCreated,
          dateUpdated = dateUpdated,
          dateViewed  = dateViewed))
        */

        val mainID      = (obj \ IDDB).asOpt[String]
        val jobType     = (obj \ JOBTYPE).asOpt[String]
        val parentID    = (obj \ PARENTID).asOpt[String]
        val jobID       = (obj \ JOBID).asOpt[String]
        val userID      = (obj \ USERID).asOpt[String]
        val status      = (obj \ STATUS).asOpt[JobState]
        val tool        = (obj \ TOOL).asOpt[String]
        val statID      = (obj \ STATID).asOpt[String]
        val watchList   = (obj \ WATCHLIST).asOpt[List[String]]
        val commentList = (obj \ COMMENTLIST).asOpt[List[String]]
        val dateCreated = (obj \ DATECREATED).asOpt[String]
        val dateUpdated = (obj \ DATEUPDATED).asOpt[String]
        val dateViewed  = (obj \ DATEVIEWED).asOpt[String]
        JsSuccess(Job(
          mainID      = BSONObjectID.generate(),  // TODO need to find out how to get the main id as it is needed for the job
          jobType     = "",
          parentID    = None,
          jobID       = "",
          userID      = BSONObjectID.generate(),
          status      = status.get,
          tool        = "",
          statID      = "",
          watchList   = None,
          commentList = None,
          dateCreated = Some(new DateTime()),
          dateUpdated = Some(new DateTime()),
          dateViewed  = Some(new DateTime())))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }


  implicit object JobWrites extends Writes[Job] {
    def writes (job : Job) : JsObject = Json.obj(
      IDDB        -> job.mainID,
      JOBTYPE     -> job.jobType,
      PARENTID    -> job.parentID,
      JOBID       -> job.jobID,
      USERID      -> job.userID,
      STATUS      -> job.status,
      TOOL        -> job.tool,
      STATID      -> job.statID,
      WATCHLIST   -> job.watchList,
      COMMENTLIST -> job.commentList,
      DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(job.dateUpdated.fold(-1L)(_.getMillis)),
      DATEVIEWED  -> BSONDateTime(job.dateViewed.fold(-1L)(_.getMillis))
    )
  }

  /**
    * Object containimport play.libs.Jsoning the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson : BSONDocument): Job = {
      Job(mainID      = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID("Null")),
          jobType     = bson.getAs[String](JOBTYPE).getOrElse("Error loading Job Type"),
          parentID    = bson.getAs[BSONObjectID](PARENTID),
          jobID       = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
          userID      = bson.getAs[BSONObjectID](USERID).getOrElse(BSONObjectID("Null")),
          status      = bson.getAs[JobState](STATUS).getOrElse(JobState.Error),
          tool        = bson.getAs[String](TOOL).getOrElse(""),
          statID      = bson.getAs[String](STATID).getOrElse(""),
          watchList   = bson.getAs[List[BSONObjectID]](WATCHLIST),
          commentList = bson.getAs[List[BSONObjectID]](COMMENTLIST),
          dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
          dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)),
          dateViewed  = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job) : BSONDocument = BSONDocument(
      IDDB        -> job.mainID,
      JOBTYPE     -> job.jobType,
      PARENTID    -> job.parentID,
      JOBID       -> job.jobID,
      USERID      -> job.userID,
      STATUS      -> job.status,
      TOOL        -> job.tool,
      STATID      -> job.statID,
      WATCHLIST   -> job.watchList,
      COMMENTLIST -> job.commentList,
      DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(job.dateUpdated.fold(-1L)(_.getMillis)),
      DATEVIEWED  -> BSONDateTime(job.dateViewed.fold(-1L)(_.getMillis))
    )
  }
}
