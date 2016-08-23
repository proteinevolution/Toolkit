package models.database

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader, BSONObjectID}

/**
 *
 * Created by snam on 23.08.16.
 */


case class JobHash(mainID : BSONObjectID,
                   hashValue : String)


// TODO decide on how to hash the jobs into the hash collection


object JobHash {

  lazy val ID = "_id"
  lazy val HASH = "hash"


  implicit object Reader extends BSONDocumentReader[JobHash] {
    override def read(bson: BSONDocument): JobHash = JobHash(
      mainID = bson.getAs[BSONObjectID](ID).getOrElse(BSONObjectID("Null")),
      bson.getAs[String](HASH).getOrElse("No matching hash value found")
    )
  }

  implicit object Writer extends BSONDocumentWriter[JobHash] {
    override def write(jobHash: JobHash): BSONDocument = BSONDocument(
      ID  ->  jobHash.mainID,
      HASH -> jobHash.hashValue
    )
  }
}
