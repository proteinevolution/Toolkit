package controllers

import javax.inject.Inject
import models.tools.ToolFactory
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }

import scala.concurrent.Future

class ModalController @Inject()(cc: ControllerComponents, toolFactory: ToolFactory) extends AbstractController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action.async {
    implicit request =>
      val tool             = toolFactory.values(toolName)
      val alignmentOptions = Json.toJson(tool.forwardAlignment.toArray)
      val multiSeqOptions  = Json.toJson(tool.forwardMultiSeq.toArray)
      modalType match {
        case "normal" =>
          Future.successful(
            Ok(
              Json.obj(
                "heading"                    -> "Forward hits",
                "showControlArea"            -> true,
                "showRadioBtnSelection"      -> true,
                "showRadioBtnSequenceLength" -> true,
                "alignmentOptions"           -> alignmentOptions,
                "multiSeqOptions"            -> multiSeqOptions
              )
            )
          )
        case "hhsuite" =>
          Future.successful(
            Ok(
              Json.obj(
                "heading"                    -> "Forward MSA (~100 most distinct sequences) to:",
                "showControlArea"            -> true,
                "showRadioBtnSelection"      -> false,
                "showRadioBtnSequenceLength" -> false,
                "alignmentOptions"           -> Json.arr("formatseq", "hhblits", "hhpred", "hhomp", "hhrepid"),
                "multiSeqOptions"            -> Json.arr()
              )
            )
          )
        case "simple" =>
          Future.successful(
            Ok(
              Json.obj(
                "heading"                    -> "Forward hits",
                "showControlArea"            -> true,
                "showRadioBtnSelection"      -> true,
                "showRadioBtnSequenceLength" -> false,
                "alignmentOptions"           -> alignmentOptions,
                "multiSeqOptions"            -> multiSeqOptions
              )
            )
          )
        case "simpler" =>
          Future.successful(
            Ok(
              Json.obj(
                "heading"                    -> "Forward hits",
                "showControlArea"            -> false,
                "showRadioBtnSelection"      -> false,
                "showRadioBtnSequenceLength" -> false,
                "alignmentOptions"           -> alignmentOptions,
                "multiSeqOptions"            -> multiSeqOptions
              )
            )
          )
        case _ => Future.successful(NoContent)
      }
  }

}
