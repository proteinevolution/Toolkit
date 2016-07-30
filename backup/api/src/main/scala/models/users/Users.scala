package models.users

import akka.actor.ActorRef

/**
  * Created by astephens on 25.02.16.
  */
class User (val userActor : ActorRef, // Which UserActor the Job belongs to
            val user_id : Long,       // The ID of the User
            val user_last_name : String, // The last name of the user
            val email : String) {     // The Email of the User
}

//User Class used for database storage
case class DBUser(val user_id : Long, val user_name_last : String, val email : String)