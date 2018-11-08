package de.proteinevolution.migrations.services

import com.github.mongobee.Mongobee
import javax.inject.Singleton

@Singleton
class MongobeeRunner(mongoDBUri: String) {

  def run(): Unit = {
    val runner = new Mongobee(mongoDBUri)
    runner.setChangeLogsScanPackage("de.proteinevolution.migrations.changelogs")
    runner.execute()
  }

}
