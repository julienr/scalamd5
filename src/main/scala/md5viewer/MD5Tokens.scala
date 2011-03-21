package md5viewer
import scala.util.parsing.combinator.token.Tokens

trait MD5Tokens extends Tokens {
  case class Keyword(chars: String) extends Token {
    override def toString = "keyword : "+chars
  }

  case class NumericLit(chars: String) extends Token {
    override def toString = "num : " + chars
  }

  case class StringLit(chars: String) extends Token {
    override def toString = "string : "+chars
  }
}
