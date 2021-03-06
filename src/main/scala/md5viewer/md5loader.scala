package md5viewer
import utils._
import scala.util.parsing.combinator._
import scala.util.matching.Regex
import scala.util.parsing.combinator.lexical._
import scala.collection.mutable.HashSet
import scala.io.Source
import collection.immutable.HashMap
import scala.util.parsing.combinator.syntactical.TokenParsers
import scala.util.parsing.combinator.token.Tokens
import scala.util.parsing.input.CharArrayReader.EofCh

object MD5Loader {
  class LoadingError(cause: String) extends RuntimeException(cause.toString)

  def removeComments (text: String) : String = {
    val regexp = """(?m)//[^"\r\n]*$""".r
    return regexp.replaceAllIn(text, "")
  }

  //Helper function, checks that expected == loaded.length and throw exception if not. Returns loaded on success
  def checkNum[T] (what: String, expected: Int, loaded: List[T]) : List[T] = {
    if (expected != loaded.length) {
      throw new LoadingError("Inconsistent "+what+"("+expected+") and loaded number: " + loaded.length)
    }
    loaded
  }

  def loadModel (file: String) : MD5Model = {
    val lines = io.Source.fromFile(file).mkString
    val p = new MD5ModelParser
    val model = p.parse(lines) match {
      case p.Success(m,_) => m
      case x => throw new Exception("Error loading : " + x)
    }
    model
  }

  def loadAnim (file: String) : MD5Anim = {
    val lines = io.Source.fromFile(file).mkString
    val p = new MD5AnimParser
    val anim = p.parse(lines) match {
      case p.Success(a,_) => a
      case x => throw new Exception("Error loading : " + x)
    }
    anim
  }

  class MD5GenericParser extends TokenParsers {
    override type Tokens = MD5Tokens
    override val lexical = new MD5Lexical

    protected val keywordCache : collection.mutable.HashMap[String, Parser[String]] = collection.mutable.HashMap.empty

    import lexical.{StringLit, NumericLit, Keyword}

    implicit def keyword(chars: String): Parser[String] =
      keywordCache.getOrElseUpdate(chars, accept(Keyword(chars)) ^^ (_.chars))

    def LBRACE = Keyword("{")
    def RBRACE = Keyword("}")
    def LPAREN = Keyword("(")
    def RPAREN = Keyword(")")
    def braces[T](p: => Parser[T]): Parser[T] = LBRACE ~> p <~ RBRACE
    def parens[T](p: => Parser[T]): Parser[T] = LPAREN ~> p <~ RPAREN

    def numericLit: Parser[String] =
      elem("number", _.isInstanceOf[NumericLit]) ^^ (_.chars)

    def stringLit: Parser[String] =
      elem("string literal", _.isInstanceOf[StringLit]) ^^ (_.chars)

    def version = keyword("MD5Version") ~> intNumber

    def commandline = keyword("commandline") ~> stringLit

    def floatNumber = numericLit ^^ { case lit => java.lang.Float.parseFloat(lit) }
    def intNumber = numericLit ^^ { _.toInt }

    def vector3 = parens(floatNumber ~ floatNumber ~ floatNumber) ^^ {
      case x ~ y ~ z => {
        new Vector3(x,y,z)
      }
    }
  }


  //A parser for MD5 model files. Calling parseAll(model, ...) will return a Model
  class MD5ModelParser extends MD5GenericParser {
    def parse(s:String) = {
      val tokens = new lexical.Scanner(s)
      phrase(model)(tokens)
    }

    def model = version ~ commandline ~ numJoints ~ numMeshes ~ joints ~ meshes ^^ {
      case v ~ cl ~ numJoints ~ numMeshes ~ joints ~ meshes => {
        new MD5Model(v, cl, checkNum("numJoints", numJoints, joints), checkNum("numMeshes", numMeshes, meshes))
      }
    }
 
    def numJoints = keyword("numJoints") ~> intNumber

    def numMeshes = keyword("numMeshes") ~> intNumber

    def joints = keyword("joints") ~> braces(rep(joint))

    var jointCnt = 0
    def joint = stringLit ~ intNumber ~ vector3 ~ vector3 ^^ {
      case name ~ parent ~ pos ~ compressedQuat => {
        val r = new Joint(jointCnt, name, parent, pos, new Quaternion(compressedQuat.x, compressedQuat.y, compressedQuat.z))
        jointCnt += 1
        r
      }
    }

    def meshes = rep(mesh)

    def mesh = keyword("mesh") ~> braces(shader ~ verts ~ tris ~ weights) ^^ {
      case shader ~ verts ~ tris ~ weights => {
        new Mesh(shader, verts, tris, weights)
      }
    }

    def shader = keyword("shader") ~> stringLit

    def verts = keyword("numverts") ~> intNumber ~ rep(vert) ^^ { case numVerts ~ verts =>
      checkNum("numVerts", numVerts, verts);
    } 

    def vert = keyword("vert") ~> intNumber ~> parens(floatNumber ~ floatNumber) ~ intNumber ~ intNumber ^^ {
      case u ~ v ~ firstWeight ~ numWeights => {
        new Vert(u, v, firstWeight, numWeights)
      }
    }

    def tris = keyword("numtris") ~> intNumber ~ rep(tri) ^^ { case numTris ~ tris =>
      checkNum("numTris", numTris, tris);
    }
    def tri = keyword("tri") ~> intNumber ~ intNumber ~ intNumber ~ intNumber ^^ {
      case triNum ~ i1 ~ i2 ~ i3 => {
        new Tri(List(i1,i2,i3).toArray)
      }
    }

    def weights = keyword("numweights") ~> intNumber ~ rep(weight) ^^ { case numWeights ~ weights =>
      checkNum("numWeights", numWeights, weights) 
    }
    def weight = keyword("weight") ~> intNumber ~ intNumber ~ floatNumber ~ vector3 ^^ {
      case weightNum ~ boneIndex ~ bias ~ w => {
        new Weight(boneIndex, bias, w)
      }
    }
  }


  class MD5AnimParser extends MD5GenericParser {
    def parse(s:String) = {
      val tokens = new lexical.Scanner(s)
      phrase(anim)(tokens)
    }

    def anim = version ~ commandline ~ numFrames ~ numJoints ~ frameRate ~ numAnimatedComponents ~ hierarchy ~ bounds ~ baseframe ~ rep(frame) ^^ {
      case version ~ commandline ~ numFrames ~ numJoints ~ frameRate ~ numAnimatedComponents ~ hierarchy ~ bounds ~ baseFrame ~ frames => {
        new MD5Anim(version, commandline, hierarchy, baseFrame, frames, frameRate)
      }
    }

    def numFrames = keyword("numFrames") ~> intNumber
    def numJoints = keyword("numJoints") ~> intNumber
    def frameRate = keyword("frameRate") ~> intNumber
    def numAnimatedComponents = keyword("numAnimatedComponents") ~> intNumber

    def hierarchy = keyword("hierarchy") ~> braces(rep(jointInfo))
    def jointInfo = stringLit ~ intNumber ~ intNumber ~ intNumber ^^ {
      case jointName ~ parent ~ flags ~ frameIndex => new JointInfo(parent, flags, frameIndex)
    }

    def bounds = keyword("bounds") ~> braces(rep(bound))
    def bound = vector3 ~ vector3 

    def baseframe = keyword("baseframe") ~> braces(rep(bframe)) ^^ {
      new BaseFrame(_)
    }
    def bframe = vector3 ~ vector3 ^^ {
      case pos ~ rot => new JointBaseFrame(pos, new Quaternion(rot.x, rot.y, rot.z))
    }

    def frame = keyword("frame") ~> intNumber ~ braces(rep(floatNumber)) ^^ { case frameNum ~ comps => new Frame(comps.toArray) }
  }
}

// vim: set ts=2 sw=2 et:
