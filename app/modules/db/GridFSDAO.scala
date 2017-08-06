package modules.db

import javax.inject.Inject

import play.api.libs.iteratee.Enumerator
import reactivemongo.api.{BSONSerializationPack, MongoConnection, MongoDriver}
import reactivemongo.api.gridfs.{DefaultFileToSave, GridFS, ReadFile}
import reactivemongo.bson.BSONValue
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.gridfs.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class GridFSDAO {


  type GFile = ReadFile[BSONSerializationPack.type, BSONValue]


  private val dbname = "tkplay_dev"
  private val uri = "mongodb://balata"
  private val driver = new MongoDriver()
  private val connection: Try[MongoConnection] = MongoConnection.parseURI(uri).map {
    parsedUri => driver.connection(parsedUri)
  }
  private val db = connection.get.db(dbname)


  def saveResultToGridFS(filename : String, contentType: Option[String], data: Enumerator[Array[Byte]]) : Future[GridFSDAO.this.GFile] = {

    saveToGridFS(GridFS[BSONSerializationPack.type](db, "resultCollection"), filename, contentType, data)

  }


  def saveToGridFS( gridfs: GridFS[BSONSerializationPack.type],
                    filename: String,
                    contentType: Option[String],
                    data: Enumerator[Array[Byte]]
                  )(implicit ec: ExecutionContext): Future[GFile] = {
    // Prepare the GridFS object to the file to be pushed
    val gridfsObj = DefaultFileToSave(Some(filename), contentType)

    gridfs.save(data, gridfsObj)
  }


}
