package models

/**
  * Created by lukas on 1/16/16.
  */
object Session {

  var counter = 0

  def next = {

    counter += 1
    counter
  }
}
