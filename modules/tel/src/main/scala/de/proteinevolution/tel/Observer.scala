/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.tel

/**
 * Stuff for the observer pattern
 *
 */
trait Observer[S] {
  def receiveUpdate(subject: S): Unit
  def receiveInitial(subject: S): Unit
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
