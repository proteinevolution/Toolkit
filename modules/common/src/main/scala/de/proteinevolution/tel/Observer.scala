package de.proteinevolution.tel

/**
  * Stuff for the observer pattern
  *
  */
trait Observer[S] {
  def receiveUpdate(subject: S)
  def receiveInitial(subject: S)
}

// TODO Add support for deleting Observers
trait Subject[S] { this: S =>
  private var observers: List[Observer[S]] = Nil

  def addObserver(observer: Observer[S]): Unit = {
    observers = observer :: observers
    observer.receiveInitial(this)
  }

  def notifyObservers(): Unit = observers.foreach(_.receiveUpdate(this))
}
