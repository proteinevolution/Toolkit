package modules.tel.env

import javax.inject.Singleton

import modules.tel.Observer
import play.api.Logger

/**
  * Manages values of Keys from the TEL environment
  *
  * Created by lzimmermann on 8/19/16.
  */
@Singleton
class TELEnv extends Env with Observer[EnvFile]    {

  private var env : Map[String, String] = Map.empty

  override def get(key : String): String = this.env(key)

  override def configure(key: String, value: String) : Unit = {
    this.env = this.env + (key -> value)
  }

  override def remove(key: String) : Unit = {
    this.env -= key
  }


  override def receiveInitial(subject: EnvFile) : Unit = receiveUpdate(subject)

  override def receiveUpdate(subject: EnvFile) : Unit = {

    // If the Environmental file triggers a change, reload it and add new variables to the
    // env
    subject.load.foreach { kv =>
      this.env = this.env + kv
    }
  }
}
