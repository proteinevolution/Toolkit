import com.google.inject.AbstractModule
import services.{ Configuration, ConfigurationImpl }

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Configuration])
      .to(classOf[ConfigurationImpl])
      .asEagerSingleton()
  }
}
