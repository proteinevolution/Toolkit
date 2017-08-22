package modules.db

import com.typesafe.config.ConfigFactory
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.{BSONSerializationPack, MongoConnection, MongoDriver}
import reactivemongo.api.gridfs.{DefaultFileToSave, GridFS, ReadFile}
import reactivemongo.bson.BSONValue
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.gridfs.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object GridFSDAO {


  type GFile = ReadFile[BSONSerializationPack.type, BSONValue]

  // this part looks quite deprecated

  private val dbname = "tkplay_dev"
  private val uri    = s"mongodb://${ConfigFactory.load().getString("elastic4s.hostname")}"  // simply identical to elastic4s.hostname
  private val driver = new MongoDriver()
  private val connection: Try[MongoConnection] = MongoConnection.parseURI(uri).map { parsedUri =>
    driver.connection(parsedUri)
  }
  private val db = connection.get.db(dbname)


  // this part is from the reactivemongo docs

  def saveResultToGridFS(filename: String,
                         contentType: Option[String],
                         data: Enumerator[Array[Byte]]): Future[GridFSDAO.this.GFile] = {

    saveToGridFS(GridFS[BSONSerializationPack.type](db), filename, contentType, data)

  }


  // this is the original method

  def saveToGridFS(gridfs: GridFS[BSONSerializationPack.type],
                   filename: String,
                   contentType: Option[String],
                   data: Enumerator[Array[Byte]])(implicit ec: ExecutionContext): Future[GFile] = {
    // Prepare the GridFS object to the file to be pushed
    val gridfsObj = DefaultFileToSave(Some(filename), contentType)

    gridfs.save(data, gridfsObj)
  }

  // well there are some more examples



}
