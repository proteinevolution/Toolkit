package modules.tel.param

/**
  * Created by lzimmermann on 10/12/16.
  */
trait Params {

  def generateValues(name: String): Map[String, String]
}
