package modules.tel

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

  def addObserver(observer: Observer[S]) = {

    observers = observer :: observers
    observer.receiveInitial(this)
  }

  def notifyObservers() = observers.foreach(_.receiveUpdate(this))
}
