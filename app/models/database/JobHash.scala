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
                   runscriptHash: String,
                   dbName : Option[String],
                   dbMtime : Option[String],
                   toolname: String,
                   toolVersion: String)


object JobHash {

  val ID = "_id"
  val INPUTHASH = "hash"
  val RUNSCRIPTHASH = "rshash"
  val DBNAME = "dbname"
  val DBMTIME = "dbmtime"
  val TOOLNAME = "toolname"
  val TOOLVERSION = "toolversion"


  implicit object Reader extends BSONDocumentReader[JobHash] {
    override def read(bson: BSONDocument): JobHash = JobHash(
      mainID = bson.getAs[BSONObjectID](ID).get,
      bson.getAs[String](INPUTHASH).getOrElse("No matching hash value found"),
      bson.getAs[String](RUNSCRIPTHASH).getOrElse("No matching hash value found"),
      bson.getAs[String](DBNAME),
      bson.getAs[String](DBMTIME),
      bson.getAs[String](TOOLNAME).getOrElse(""),
      bson.getAs[String](TOOLVERSION).getOrElse("")
    )
  }

  implicit object Writer extends BSONDocumentWriter[JobHash] {
    override def write(jobHash: JobHash): BSONDocument = BSONDocument(
      ID  ->  jobHash.mainID,
      INPUTHASH -> jobHash.inputHash,
      RUNSCRIPTHASH -> jobHash.runscriptHash,
      DBNAME -> jobHash.dbName,
      DBMTIME -> jobHash.dbMtime,
      TOOLNAME -> jobHash.toolname,
      TOOLVERSION -> jobHash.toolVersion
    )
  }
}
