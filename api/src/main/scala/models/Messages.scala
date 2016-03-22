package models

import models.jobs.UserJob

/**
  * Created by lzimmermann on 22.03.16.
  */
object Messages {


  case class UpdateWDDone(userJob : UserJob)

}
