/*
rule = Parser01Fix
*/
package fix

import cats.data.NonEmptyList
import cats.parse.Parser.{string1, rep1}
import cats.parse.Parser
import cats.parse.Parser1
import Parser.{ignoreCase1, char}

object Http4sIdioms {

  val x: Parser1[NonEmptyList[Unit]] = rep1(char('a') *> char('b') *> char('c'), 1)
  val y: Parser1[String] = string1(x)
  val z = ignoreCase1("FooBar")
}