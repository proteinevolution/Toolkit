package modules.tel.param

import javax.inject.Singleton

/**
  * Created by lzimmermann on 10/12/16.
  */
@Singleton
class ParamCollector extends Params {

  // Maps Parameter name to the underlying object
  private var generativeParams: Map[String, GenerativeParam] = Map.empty

  def generateValues(name: String): Map[String, String] = generativeParams(name).generate

  def addParam(name: String, param: GenerativeParam): Unit = {

    this.generativeParams = this.generativeParams + (name -> param)
  }
}
