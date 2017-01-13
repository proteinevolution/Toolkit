package modules.analysis.strings

/**
  * Computes the longest common substring with dynamic programming
  *
  *  scala> lcsM("thisiaatest".toList, "testing123testing".toList).mkString
  *  res0: String = tsitest
  *
  * Created by zin on 13.01.17.
  */

object LCS {

  case class Memoized[A1, A2, B](f: (A1, A2) => B) extends ((A1, A2) => B) {
    val cache = scala.collection.mutable.Map.empty[(A1, A2), B]

    def apply(x: A1, y: A2) = cache.getOrElseUpdate((x, y), f(x, y))
  }

  lazy val lcsM : Memoized[List[Char], List[Char], List[Char]] = Memoized {
    case (_, Nil) => Nil
    case (Nil, _) => Nil
    case (x :: xs, y :: ys) if x == y => x :: lcsM(xs, ys)
    case (x :: xs, y :: ys) => {
      (lcsM(x :: xs, ys), lcsM(xs, y :: ys)) match {
        case (`xs`, `ys`) if `xs`.length > `ys`.length => xs
        case (`xs`, `ys`) => `ys`
      }
    }
  }
}