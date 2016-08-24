package models.database

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader, BSONObjectID}

/**
 *
 * Created by snam on 23.08.16.
 *
 * the JobHash also contains non-hashed information to avoid collision as much as possible. Only the input file and the parameters will be hashed
 * due performance reasons. Note that not every tool/job uses a database.
 *
 */


case class JobHash(mainID : BSONObjectID,
                   inputHash : String,
                   dbName : Option[String],
                   dbMtime : Option[String])


object JobHash {

  lazy val ID = "_id"
  lazy val INPUTHASH = "hash"
  lazy val DBNAME = "dbname"
  lazy val DBMTIME = "dbmtime"


  implicit object Reader extends BSONDocumentReader[JobHash] {
    override def read(bson: BSONDocument): JobHash = JobHash(
      mainID = bson.getAs[BSONObjectID](ID).getOrElse(BSONObjectID("Null")),
      bson.getAs[String](INPUTHASH).getOrElse("No matching hash value found"),
      bson.getAs[String](DBNAME),
      bson.getAs[String](DBMTIME)
    )
  }

  implicit object Writer extends BSONDocumentWriter[JobHash] {
    override def write(jobHash: JobHash): BSONDocument = BSONDocument(
      ID  ->  jobHash.mainID,
      INPUTHASH -> jobHash.inputHash,
      DBNAME -> jobHash.dbName,
      DBMTIME -> jobHash.dbMtime
    )
  }
}
