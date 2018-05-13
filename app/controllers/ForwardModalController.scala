package controllers

import de.proteinevolution.models.forwarding.ForwardingError.InvalidModal
import de.proteinevolution.models.forwarding.{ ForwardModalOptions, ForwardingError }
import javax.inject.{ Inject, Singleton }
import models.tools.ToolFactory
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }

@Singleton
class ForwardModalController @Inject()(cc: ControllerComponents, toolFactory: ToolFactory)
    extends AbstractController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action { implicit request =>
    val tool             = toolFactory.values(toolName)
    val alignmentOptions = tool.forwardAlignment.toArray
    val multiSeqOptions  = tool.forwardMultiSeq.toArray

    val options: Either[ForwardingError, ForwardModalOptions] = modalType match {
      case "normal" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = true,
            showRadioBtnSequenceLength = true,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case "hhsuite" =>
        Right(
          ForwardModalOptions(
            "Forward MSA (~100 most distinct sequences) to:",
            showRadioBtnSelection = false,
            showRadioBtnSequenceLength = false,
            Array("formatseq", "hhblits", "hhpred", "hhomp", "hhrepid"),
            Array.empty
          )
        )
      case "simple" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = true,
            showRadioBtnSequenceLength = false,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case "simpler" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = false,
            showRadioBtnSequenceLength = false,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case _ => Left(InvalidModal)
    }

    options match {
      case Right(o) => Ok(Json.toJson(o))
      case Left(e)  => BadRequest(e.message)
    }

  }

}
