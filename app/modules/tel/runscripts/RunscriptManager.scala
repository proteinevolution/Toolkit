package modules.tel.runscripts

import javax.inject.{Inject, Singleton}


/**
  * Created by lzimmermann on 10/19/16.
  */
@Singleton
class RunscriptManager @Inject() (types : Types) {

  // Maps the name of each runscript to the corresponding class
  private var runscripts : Map[String, Runscript] = Map.empty




  def addRunscript(name : String, path : String) : Unit = {

    this.runscripts = this.runscripts.updated(name, Runscript(path))
  }




}
