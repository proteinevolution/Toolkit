package models.datatables

import javax.inject.Inject

import models.database.results.datatables.PSIBlastDT
import models.datatables.HitListDAL.PSIBlastDTParam
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by snam on 18.04.17.
  */

class HitlistDAL @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends CommonModule {

  def getHits(jobID : String, numiter : Int): Future[List[PSIBlastDT]] = {
    getPsiblastResult(jobID, numiter).map {

      x => println(x.toString) ;x
    }
  }


  def getHitsByKeyWord(jobID: String, numiter: Int, params: PSIBlastDTParam): Future[List[PSIBlastDT]] = {
    params.sSearch.isEmpty match {
      case true => getHits(jobID, numiter).map { x =>
        x.slice(params.iDisplayStart,params.iDisplayLength)
      }
      case false => ???
    }
  }

}


object HitListDAL{

  case class PSIBlastDTParam(sSearch: String, iDisplayStart: Int, iDisplayLength: Int, iSortCol: Int, sSortDir: String)


}
