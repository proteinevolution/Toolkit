package de.proteinevolution.migrations.services

import com.github.mongobee.Mongobee
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

@Singleton
final class MongobeeRunner @Inject()(config: Configuration) {

  def run(): Unit = {
    val runner = new Mongobee(config.get[String]("mongodb.uri"))
    runner.setChangeLogsScanPackage("de.proteinevolution.migrations.changelogs")
    runner.execute()
  }

}
