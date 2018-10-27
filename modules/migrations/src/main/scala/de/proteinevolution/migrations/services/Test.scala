package de.proteinevolution.migrations.services
import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class Test @Inject()(
    configuration: Configuration
) {

  val runner = new Mongobee(configuration.get[String]("mongodb.uri"))
  runner.setChangeLogsScanPackage("de.proteinevolution.migrations.changelogs")
  runner.execute()

}
