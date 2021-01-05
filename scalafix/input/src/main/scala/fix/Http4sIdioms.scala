/*
rule = Parser01Fix
*/
package fix

import cats.data.NonEmptyList
import cats.parse.Parser.{string1, rep1, ignoreCase1, char}
import cats.parse.{Parser1 => P1}

object Http4sIdioms {

  val x: P1[NonEmptyList[Unit]] = rep1(char('a') *> char('b') *> char('c'), 1)
  val y: P1[String] = string1(x)
  val z = ignoreCase1("FooBar")
}