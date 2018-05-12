package controllers

import de.proteinevolution.models.util.ForwardModalOptions
import javax.inject.Inject
import models.tools.ToolFactory
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }

class ModalController @Inject()(cc: ControllerComponents, toolFactory: ToolFactory) extends AbstractController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action { implicit request =>
    val tool             = toolFactory.values(toolName)
    val alignmentOptions = tool.forwardAlignment.toArray
    val multiSeqOptions  = tool.forwardMultiSeq.toArray
    val options = modalType match {
      case "normal" =>
        ForwardModalOptions(
          "Forward hits",
          showControlArea = true,
          showRadioBtnSelection = true,
          showRadioBtnSequenceLength = true,
          alignmentOptions,
          multiSeqOptions
        )
      case "hhsuite" =>
        ForwardModalOptions(
          "Forward MSA (~100 most distinct sequences) to:",
          showControlArea = true,
          showRadioBtnSelection = false,
          showRadioBtnSequenceLength = false,
          Array("formatseq", "hhblits", "hhpred", "hhomp", "hhrepid"),
          Array.empty
        )
      case "simple" =>
        ForwardModalOptions(
          "Forward hits",
          showControlArea = true,
          showRadioBtnSelection = true,
          showRadioBtnSequenceLength = false,
          alignmentOptions,
          multiSeqOptions
        )
      case "simpler" =>
        ForwardModalOptions(
          "Forward hits",
          showControlArea = false,
          showRadioBtnSelection = false,
          showRadioBtnSequenceLength = false,
          alignmentOptions,
          multiSeqOptions
        )
    }
    Ok(Json.toJson(options))
  }

}
