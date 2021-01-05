package fix

import cats.data.NonEmptyList
import cats.parse.Parser.string
import cats.parse.Parser0
import cats.parse.Parser
import Parser.{ignoreCase, char}

object Http4sIdioms {

  val x: Parser[NonEmptyList[Unit]] = (char('a') *> char('b') *> char('c')).rep
  val y: Parser[String] = string(x)
  val z = ignoreCase("FooBar")
}
