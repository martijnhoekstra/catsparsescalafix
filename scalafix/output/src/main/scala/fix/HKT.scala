package fix

import cats._
import cats.parse.Parser0
import cats.parse.{Parser => P}

object HKT {
  val p1Functor: Functor[Parser0] = new Functor[Parser0] {
    def map[A, B](fa: Parser0[A])(f: A => B): Parser0[B] = fa.map(f)
  }
  val p0Functor: Functor[P] = new Functor[P]{
    def map[A, B](fa: P[A])(f: A => B): P[B] = fa.map(f)
  }
}