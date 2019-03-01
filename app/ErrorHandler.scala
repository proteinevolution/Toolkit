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

import controllers.AssetsFinder
import de.proteinevolution.base.helpers.ToolkitTypes
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.{ Inject, Singleton }

@Singleton
final class ErrorHandler @Inject()(assets: AssetsFinder) extends HttpErrorHandler with ToolkitTypes {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    fuccess(Status(statusCode)(views.html.errors.pagenotfound(assets)))
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    fuccess(InternalServerError(exception.toString))
  }
}
