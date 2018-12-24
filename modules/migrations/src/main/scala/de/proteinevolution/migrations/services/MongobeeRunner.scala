package de.proteinevolution.migrations.services

import com.github.mongobee.Mongobee

object MongobeeRunner {

  def run(mongoDBUri: String): Unit = {
    val runner = new Mongobee(mongoDBUri)
    runner.setChangeLogsScanPackage("de.proteinevolution.migrations.changelogs")
    runner.execute()
  }

}
