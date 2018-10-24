package de.proteinevolution.base.helpers

import scala.concurrent.Future

trait ToolkitTypes {

  type Fu[A] = Future[A]

  @inline def fuccess[A](a: A): Fu[A] = Future.successful(a)

  implicit class ToFutureSuccessful[T](obj: T) {
    @inline def asFuture: Future[T] = Future.successful(obj)
  }

}

object ToolkitTypes extends ToolkitTypes {

  import shapeless._

  trait AllSingletons[A, C <: Coproduct] {
    def values: List[A]
  }

  object AllSingletons {
    implicit def cnilSingletons[A]: AllSingletons[A, CNil] =
      new AllSingletons[A, CNil] {
        def values: Nil.type = Nil
      }

    implicit def coproductSingletons[A, H <: A, T <: Coproduct](
        implicit
        tsc: AllSingletons[A, T],
        witness: Witness.Aux[H]
    ): AllSingletons[A, H :+: T] =
      new AllSingletons[A, H :+: T] {
        def values: List[A] = witness.value :: tsc.values
      }
  }

  trait EnumerableAdt[A] {
    def values: Set[A]
  }

  object EnumerableAdt {
    implicit def fromAllSingletons[A, C <: Coproduct](
        implicit
        gen: Generic.Aux[A, C],
        singletons: AllSingletons[A, C]
    ): EnumerableAdt[A] =
      new EnumerableAdt[A] {
        def values: Set[A] = singletons.values.toSet
      }
  }

}
