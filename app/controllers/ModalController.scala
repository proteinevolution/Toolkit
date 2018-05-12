package controllers

import javax.inject.Inject
import models.tools.ToolFactory
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

class ModalController @Inject()(cc: ControllerComponents, toolFactory: ToolFactory) extends AbstractController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] =  Action { implicit request =>
      val tool             = toolFactory.values(toolName)
      val alignmentOptions = tool.forwardAlignment.toArray
      val multiSeqOptions  = tool.forwardMultiSeq.toArray
      modalType match {
        case "normal" =>
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

        case "hhsuite" =>
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
        case "simple" =>
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
        case "simpler" =>
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
        case _ => BadRequest
      }
  }

}
