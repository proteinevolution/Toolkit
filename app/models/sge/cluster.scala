package models.sge

import javax.inject.{Inject, Singleton}


/**
  * Created by snam on 19.03.17.
  */

@Singleton
class Cluster @Inject()(qhost : Qhost,
                        qstat : Qstat) {




  def getLoad() : Unit = {

    qhost.get()

  }


}


object cluster {




}
