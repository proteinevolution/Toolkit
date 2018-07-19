package modules

import com.google.inject.AbstractModule
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tel.param.Params
import com.google.inject.name.Names
import de.proteinevolution.tel.{
  ParamCollectorProvider,
  RunscriptPathProvider,
  WrapperPathProvider
}

class TELModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Env]).toProvider(classOf[TELEnvProvider]).asEagerSingleton()
    bind(classOf[Params])
      .toProvider(classOf[ParamCollectorProvider])
      .asEagerSingleton()
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
