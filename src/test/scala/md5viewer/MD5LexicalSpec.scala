package test.scala.md5viewer
import md5viewer._
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

class MD5LexicalSpec extends WordSpec with ShouldMatchers {
  val lex = new MD5Lexical

  def tokenize(input: String) = new lex.Scanner(input)

  "MD5Lexical" should {
    "parse an signed integer" in {
      tokenize("-435").first match {
        case lex.NumericLit(v) => v should equal("-435")
        case _ => fail("Not recognized")
      }
    }

    "parse a signed float" in {
      tokenize("-42.53").first match {
        case lex.NumericLit(v) => v should equal("-42.53")
        case _ => fail("Not recognized")
      }
    }

    "parse a simple line" in {
      var tokens = tokenize("""MD5Version 43 blah "teststring" second { } 43""")
      def next = { tokens = tokens.rest; tokens.first }
      tokens.first should equal(lex.Keyword("MD5Version"))
      next should equal(lex.NumericLit("43"))
      next should equal(lex.Keyword("blah"))
      next should equal(lex.StringLit("teststring"))
      /*while(!tokens.atEnd) {
        Console.println(tokens.first)
        tokens = tokens.rest
      }*/
    }
  }
}