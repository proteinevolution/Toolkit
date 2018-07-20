package de.proteinevolution.base.helpers

import scala.concurrent.Future

trait ToolkitTypes {

  type Fu[A] = Future[A]

  @inline def fuccess[A](a: A): Fu[A] = Future.successful(a)

  implicit class ToFutureSuccessful[T](obj: T) {
    @inline def asFuture: Future[T] = Future.successful(obj)
  }

}

object ToolkitTypes extends ToolkitTypes
