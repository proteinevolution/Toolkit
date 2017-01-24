package models.database

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

/**
  * Created by lzimmermann on 24.01.17.
  */


object Results {

  case class Hit(no: Int,
                 hit: String,
                 prob: Double,
                 eval: Double,
                 pval: Double,
                 score: Double,
                 ss: Double,
                 cols: Int,
                 query_begin: Int,
                 query_end: Int,
                 template_begin: Int,
                 template_end: Int,
                 ref: Int)

  implicit val hitsRead: Reads[Hit] = (
      (JsPath \ "no").read[Int] and
      (JsPath \ "hit").read[String] and
      (JsPath \ "prob").read[Double] and
      (JsPath \ "eval").read[Double] and
      (JsPath \ "pval").read[Double] and
      (JsPath \ "score").read[Double] and
      (JsPath \ "ss").read[Double] and
      (JsPath \ "cols").read[Int] and
      (JsPath \ "query_begin").read[Int] and
      (JsPath \ "query_end").read[Int] and
      (JsPath \ "template_begin").read[Int] and
      (JsPath \ "template_end").read[Int] and
      (JsPath \ "ref").read[Int]
    )(Hit.apply _)
}
