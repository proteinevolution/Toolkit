/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import de.proteinevolution.migrations.services.MongobeeRunner
import play.api.ApplicationLoader
import play.api.ApplicationLoader.Context
import play.api.inject.bind
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceApplicationLoader, GuiceableModule }
import play.api.libs.concurrent.MaterializerProvider

class ToolkitAppLoader extends GuiceApplicationLoader {

  private val materializerOverrides: Seq[GuiceableModule] = Seq(
    bind[Materializer].toProvider[MaterializerProvider]
  )

  private val config = ConfigFactory.load()

  private val mongoUri = config.getString("mongodb.uri")

  protected override def overrides(context: ApplicationLoader.Context): Seq[GuiceableModule] = {
    GuiceApplicationLoader.defaultOverrides(context) ++ materializerOverrides
  }

  override def builder(context: Context): GuiceApplicationBuilder = {
    MongobeeRunner.run(mongoUri)
    super.builder(context)
  }

}
