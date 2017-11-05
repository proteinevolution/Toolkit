import com.google.inject.AbstractModule
import services.{ Configuration, ConfigurationImpl }

class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[Configuration]).to(classOf[ConfigurationImpl]).asEagerSingleton()

  }

}
