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

package services

import javax.inject._
import play.Environment
import play.api.Logging
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

sealed trait Configuration

@Singleton
final class ConfigurationImpl @Inject()(
    appLifecycle: ApplicationLifecycle,
    ws: WSClient,
    environment: Environment
) extends Configuration
    with Logging {

  private def init(): Unit = {
    if (environment.isProd) {
      appLifecycle.addStopHook { () =>
        logger.info("configuring hostname .... ")
        ws.url("https://toolkit.tuebingen.mpg.de").execute("GET")
      }
    }
  }

  init()

}
