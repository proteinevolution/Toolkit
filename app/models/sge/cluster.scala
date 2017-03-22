package models.sge

import javax.inject.{Inject, Singleton}


/**
  * Created by snam on 19.03.17.
  */

@Singleton
class Cluster @Inject()(qhost : Qhost) {

  def getLoad() : Unit = {

    println("TEST")

  }


}


object cluster {




}
