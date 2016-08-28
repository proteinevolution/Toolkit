package modules

import com.google.inject.AbstractModule
import models.tel.env.{Env, TELEnv}

/**
  * Created by lukas on 8/28/16.
  */
class TELModule  extends AbstractModule {


  override def configure() = {

        bind(classOf[Env]).to(classOf[TELEnv])
  }
}
