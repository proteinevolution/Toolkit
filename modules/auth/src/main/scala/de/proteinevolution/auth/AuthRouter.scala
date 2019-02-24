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

package de.proteinevolution.auth

import de.proteinevolution.auth.controllers.{ AuthController, ValidationController }
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class AuthRouter @Inject()(authController: AuthController, validationController: ValidationController)
    extends SimpleRouter {

  private lazy val authRoutes: Routes = {
    case GET(p"/signout")                => authController.signOut
    case GET(p"/user/data")              => authController.getUserData
    case POST(p"/signup")                => authController.signUpSubmit
    case POST(p"/reset/password")        => authController.resetPassword
    case POST(p"/reset/password/change") => authController.resetPasswordChange
    case POST(p"/password")              => authController.passwordChangeSubmit()
    case POST(p"/profile")               => authController.profileSubmit()
    case POST(p"/signin")                => authController.signInSubmit
  }

  private lazy val validationRoutes: Routes = {
    case POST(p"/validate/modeller" ? q_o"input=$input") => validationController.validateModellerKey(input)
  }

  override def routes: Routes = {
    authRoutes.orElse(validationRoutes)
  }

}
