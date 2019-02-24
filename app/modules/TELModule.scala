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

package modules

import com.google.inject.AbstractModule
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tel.param.Params
import com.google.inject.name.Names
import de.proteinevolution.tel.{ ParamCollectorProvider, RunscriptPathProvider, WrapperPathProvider }

class TELModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Env]).toProvider(classOf[TELEnvProvider]).asEagerSingleton()
    bind(classOf[Params]).toProvider(classOf[ParamCollectorProvider]).asEagerSingleton()
    bind(classOf[String])
      .annotatedWith(Names.named("runscriptPath"))
      .toProvider(classOf[RunscriptPathProvider])
      .asEagerSingleton()
    bind(classOf[String])
      .annotatedWith(Names.named("wrapperPath"))
      .toProvider(classOf[WrapperPathProvider])
      .asEagerSingleton()
  }

}
