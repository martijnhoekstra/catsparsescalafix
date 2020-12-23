/*
rule = Parser01Fix
*/
package fix

import cats._
import cats.parse.Parser
import cats.parse.{Parser1 => P1}

object HKT {
  val p1Functor: Functor[Parser] = new Functor[Parser] {
    def map[A, B](fa: Parser[A])(f: A => B): Parser[B] = fa.map(f)
  }
  val p0Functor: Functor[P1] = new Functor[P1]{
    def map[A, B](fa: P1[A])(f: A => B): P1[B] = fa.map(f)
  }
}