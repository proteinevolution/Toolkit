package modules.tel.param

import javax.inject.Singleton

import scala.collection.immutable.ListMap

/**
  * Created by lzimmermann on 10/12/16.
  */
@Singleton
class ParamCollector extends Params {

  // Maps Parameter name to the underlying object
  private var generativeParams: ListMap[String, GenerativeParam] = ListMap.empty

  def generateValues(name: String): ListMap[String, String] = generativeParams(name).generate

  def addParam(name: String, param: GenerativeParam): Unit = {

    this.generativeParams = this.generativeParams + (name -> param)
  }
}
