/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.forwarding.ForwardingError.InvalidModal
import de.proteinevolution.common.models.forwarding.{ ForwardModalOptions, ForwardingError }
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import io.circe.syntax._

@Singleton
class ForwardModalController @Inject()(cc: ControllerComponents, toolConfig: ToolConfig) extends ToolkitController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action { implicit request =>
    val tool             = toolConfig.values(toolName)
    val alignmentOptions = tool.toolParameterForm.forwarding.alignment.toArray
    val multiSeqOptions  = tool.toolParameterForm.forwarding.multi_seq.toArray

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
      case Right(o) => Ok(o.asJson)
      case Left(e)  => BadRequest(e.message)
    }

  }

}
