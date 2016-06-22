package models.misc

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import scala.util.Try

/**
  * Utility Object for handling serialized arrays from and to json
  *
  * Usage: val jsonString = JSONUtil.toJSON(myObject)
  * val myNewObject: Option[MyClass] = JSONUtil.fromJSONOption[MyClass](aJsonString)
  *
  * Created by zin on 12.03.16.
  */

object JSONUtil {

  implicit val formats = DefaultFormats

  def toJSON(objectToWrite: AnyRef): String = Serialization.write(objectToWrite)

  def fromJSONOption[T](jsonString: String)(implicit mf: Manifest[T]): Option[T] = Try(Serialization.read(jsonString)).toOption

}