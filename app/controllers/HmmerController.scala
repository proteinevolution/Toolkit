package controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import modules.CommonModule
import models.database.results._
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future

/**
  * Created by drau on 18.04.17.
  */
class HmmerController @Inject() (val reactiveMongoApi : ReactiveMongoApi) extends Controller with CommonModule {

  def evalHmmer(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getFasEval(Hmmer.parseHmmerResult(jsValue), eval.toDouble))
        case _=> NotFound
      }


  }


  def fasHmmer(jobID : String, numList : Seq[Int]): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getFas(Hmmer.parseHmmerResult(jsValue), numList))
        case _ => NotFound
      }
  }

  def getFasEval(result : HmmerResult, eval : Double): String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        ">" + hit.accession + "\n" + hit.hit_seq + "\n"
      }
    }
    fas.mkString
  }

  def getFas(result : HmmerResult, list: Seq[Int]): String = {
    val fas = list.map { num =>
        ">" + result.HSPS(num -1 ).accession + "\n" + result.HSPS(num - 1).hit_seq + "\n"
      }
    fas.mkString
  }

}
