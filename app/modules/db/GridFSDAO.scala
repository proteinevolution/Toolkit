package modules.db

import reactivemongo.api.BSONSerializationPack
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONValue
import scala.concurrent.duration.FiniteDuration

class GridFSDAO {


  type GFile = ReadFile[BSONSerializationPack.type, BSONValue]




}
