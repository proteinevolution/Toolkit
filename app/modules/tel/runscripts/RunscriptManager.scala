package modules.tel.runscripts

import javax.inject.{Inject, Named, Singleton}

import play.api.Logger

/**
  * Class watches the directory of runscripts and monitors changes. Reloads information about runscripts once
  * the file changes.
  *
  * Created by lzimmermann on 10/19/16.
  */
@Singleton
class RunscriptManager @Inject() (@Named("runscriptPath") runscriptPath : String) {


    Logger.info("Going to use " + runscriptPath)
}