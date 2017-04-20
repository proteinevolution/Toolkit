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

  def alnEvalHmmer(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getAlnEval(Hmmer.parseHmmerResult(jsValue), General.parseAlignment(jsValue), eval.toDouble))
        case _=> NotFound
      }
  }


  def alnHmmer(jobID : String, numList : Seq[Int]): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getAln(General.parseAlignment(jsValue), numList))
        case _ => NotFound
      }
  }

  def getAlnEval(result : HmmerResult, alignment: Alignment, eval : Double): String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        ">" + alignment.alignment(hit.num -1).accession + "\n" + alignment.alignment(hit.num-1).seq + "\n"
      }
    }
    fas.mkString
  }

  def getAln(alignment : Alignment, list: Seq[Int]): String = {
    val fas = list.map { num =>
        ">" + alignment.alignment(num - 1).accession + "\n" + alignment.alignment(num -1 ).seq + "\n"
      }
    fas.mkString
  }

}
