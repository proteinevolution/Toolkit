package models.tel.env

import javax.inject.Singleton

import models.tel.Observer

/**
  * Manages values of Keys from the TEL environment
  *
  * Created by lzimmermann on 8/19/16.
  */
@Singleton
class TELEnv extends Env with Observer[EnvFile]    {

  private var env : Map[String, String] = Map.empty

  def get(key : String) = this.env(key)


  override def receiveUpdate(subject: EnvFile): Unit = {

    // If the Environmental file triggers a change, reload it and add new variables to the
    // env
    subject.load.foreach { kv =>
      this.env = this.env + kv
    }
  }
}
