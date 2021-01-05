package fix

import cats.data.NonEmptyList
import cats.parse.Parser.{string, ignoreCase, char}
import cats.parse.{Parser => P}

object Http4sIdioms {

  val x: P[NonEmptyList[Unit]] = (char('a') *> char('b') *> char('c')).rep(1)
  val y: P[String] = string(x)
  val z = ignoreCase("FooBar")
}
