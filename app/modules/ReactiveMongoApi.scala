package modules

import reactivemongo.api.{ DB, MongoConnection, MongoDriver }

trait ReactiveMongoApi {

  val driver = new reactivemongo.api.MongoDriver
  val connection = driver.connection(List("balata"))


}



