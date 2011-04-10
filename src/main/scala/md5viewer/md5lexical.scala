package md5viewer

import scala.collection.mutable.HashSet
import scala.util.parsing.combinator.lexical.Lexical
import scala.util.parsing.input.CharArrayReader.EofCh

class MD5Lexical extends Lexical with MD5Tokens {
  def token: Parser[Token] = 
    ( identChar ~ rep( identChar | digit )             ^^ { case first ~ rest => Keyword(first :: rest mkString) }
     | '\"' ~ rep(chrExcept('\"', '\n', EofCh)) ~ '\"' ^^ { case '\"' ~ chars ~ '\"' => StringLit(chars mkString "") }
     | EofCh                                           ^^^ EOF
     | '\"' ~> failure("unclosed string literal")
     | delim
     | optSign ~ rep(digit) ~ optFrac                  ^^ { case sign ~ first ~ rest => NumericLit((sign :: first mkString) + rest) }
     | failure("illegal character")
    )

  // legal identifier chars other than digits
  def identChar = letter | elem('_')

  def chr(c:Char) = elem("", ch => ch==c )
  def chrOf(l:Char*) = elem("", ch =>l.contains(ch))

  def fraction = elem('.') ~ rep(digit)
  def optFrac = opt(fraction) ^^ {
    case None => ""
    case Some('.' ~ frac) => "." + (frac mkString)
  }
  
  def sign = chr('+') | chr('-')
  def optSign = opt(sign) ^^ {
    case None => ""
    case Some(sign) => sign
  }

  lazy val lineComment: Parser[Any] = '/' ~> '/' ~> rep( chrExcept('\n', EofCh) )
  override def whitespaceChar: Parser[Char] = chrOf(' ', '\t', '\r', '\n')
  lazy val allWhitespace: Parser[String] = rep(whitespaceChar | lineComment) ^^ (_.mkString)
  
  def whitespace: Parser[Any] = allWhitespace

  // starting with the longest one -- otherwise a delimiter D will never be matched if there is
  // another delimiter that is a prefix of D
  val delimiters = List("{","}","(",")")

  private lazy val _delim: Parser[Token] = {
    def parseDelim(s: String): Parser[Token] = accept(s.toList) ^^ { x => Keyword(s) }
    (delimiters map parseDelim).foldRight(failure("no matching delimiter"): Parser[Token])((x, y) => y | x)
  }
  protected def delim: Parser[Token] = _delim
}