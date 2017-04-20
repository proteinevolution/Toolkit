package models.datatables

import javax.inject.Inject

import models.datatables.HitListDAL.PSIBlastDTParam
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi
import models.database.results.{PSIBlast, PSIBlastHSP}


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by snam on 18.04.17.
  */

class HitlistDAL @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends CommonModule {


  def getHits(jobID : String) : Future[List[PSIBlastHSP]] = {
    getResult(jobID).map {

      x => PSIBlast.parsePSIBlastResult(x.get).HSPS

    }
  }


  def getHitsByKeyWord(jobID: String, params: PSIBlastDTParam) = {
    params.sSearch.isEmpty match {
      case true => getHits(jobID).map { x => x
        //x.slice(params.iDisplayStart,params.iDisplayLength)
      }
      //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
    }
  }

}


object HitListDAL{

  case class PSIBlastDTParam(sSearch: String, iDisplayStart: Int, iDisplayLength: Int, iSortCol: Int, sSortDir: String)

}
