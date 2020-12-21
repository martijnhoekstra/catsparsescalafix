package fix

import cats.parse.Parser
import cats.parse.Parser0
import cats.parse.Numbers
/** Parsers for the common rules of RFC5234. These rules are
  * referenced by several RFCs.
  *
  * @see [[https://tools.ietf.org/html/rfc5234]]
  */
object Rfc5234 {

  /** A-Z and a-z, without diacritics
    */
  val alpha: Parser[Char] =
    Parser.charIn('A' to 'Z').orElse(Parser.charIn('a' to 'z'))

  /** `0` or `1`
    */
  val bit: Parser[Char] =
    Parser.charIn('0' to '1')

  /** any 7-bit US-ASCII character, excluding NUL
    */
  val char: Parser[Char] =
    Parser.charIn(0x01.toChar to 0x7f.toChar)

  /** carriage return
    */
  val cr: Parser[Unit] =
    Parser.char('\r')

  /** linefeed
    */
  val lf: Parser[Unit] =
    Parser.char('\n')

  /** Internet standard newline */
  val crlf: Parser[Unit] =
    Parser.string("\r\n")

  /** controls */
  val ctl: Parser[Char] =
    Parser.charIn(0x7f, (0x00.toChar to 0x1f.toChar): _*)

  /** `0` to `9`
    */
  val digit: Parser[Char] =
    Numbers.digit

  /** double quote (`"`)
    */
  val dquote: Parser[Unit] =
    Parser.char('"')

  /** hexadecimal digit, case insensitive
    */
  val hexdig: Parser[Char] =
    digit.orElse(Parser.ignoreCaseCharIn('A' to 'F'))

  /** horizontal tab
    */
  val htab: Parser[Unit] =
    Parser.char('\t')

  /** space */
  val sp: Parser[Unit] =
    Parser.char(' ')

  /** white space (space or horizontal tab) */
  val wsp: Parser[Unit] =
    sp.orElse(htab)

  /** linear white space.
    *
    * Use of this rule permits lines containing only white space that
    * are no longer legal in mail headers and have caused
    * interoperability problems in other contexts.
    *
    * Do not use when defining mail headers and use with caution in
    * other contexts.
    */
  val lwsp: Parser0[Unit] = {
    val inner = wsp.orElse(crlf *> wsp)
    inner.rep0.void
  }

  /** 8 bits of data
    */
  val octet: Parser[Char] =
    Parser.charIn(0x00.toChar to 0xff.toChar)

  /** visible (printing) characters
    */
  val vchar: Parser[Char] =
    Parser.charIn(0x21.toChar to 0x7e.toChar)

  val digits = digit.rep

  val foo = Parser.repSep(digits, 2, octet)
  val bar = Parser.rep0Sep(digits, 2, octet)
}
