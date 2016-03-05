package models.users


/**
  * Created by astephens on 25.02.16.
  */
class UserJob (val user_id   : Long,      // The ID of the User
               val job_id    : String,    // The ID of the Job
               val main_user : Boolean) { // User is the main user
}

//User Class used for database storage
case class DBUserJob(val user_id : Long, val job_id : String, val main_user : Boolean)