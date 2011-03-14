package md5viewer
import utils._
import scala.util.parsing.combinator._
import scala.util.matching.Regex

object MD5Loader {
  class LoadingError(cause: String) extends RuntimeException(cause.toString)

  def removeComments (text: String) : String = {
    val regexp = """//.*\n""".r
    return regexp.replaceAllIn(text, "\n")
  }

  //MD5 store a compressed quaternion (only x, y and z). Have to compute R
  private def computeR (cq: Vector3) : Float = {
    val t = 1.0f-(cq.x*cq.x)-(cq.y*cq.y)-(cq.z*cq.z)
    if (t < 0.0f) 0.0f
    else -scala.math.sqrt(t).toFloat
  }

  //Helper function, checks that expected == loaded.length and throw exception if not. Returns loaded on success
  def checkNum[T] (what: String, expected: Int, loaded: List[T]) : List[T] = {
    if (expected != loaded.length) {
      throw new LoadingError("Inconsistent "+what+"("+expected+") and loaded number: " + loaded.length)
    }
    loaded
  }


  //A parser for MD5 model files. Calling parseAll(model, ...) will return a Model
  class MD5ParserCombinators extends JavaTokenParsers {
    def model = version ~ commandline ~ numJoints ~ numMeshes ~ joints ~ meshes ^^ {
      case v ~ cl ~ numJoints ~ numMeshes ~ joints ~ meshes => {
        new MD5Model(v, cl, checkNum("numJoints", numJoints, joints), checkNum("numMeshes", numMeshes, meshes))
      }
    }

    def version = "MD5Version" ~> natNumber

    def commandline = "commandline" ~> quotedString

    def numJoints = "numJoints" ~> natNumber

    def numMeshes = "numMeshes" ~> natNumber

    def joints = "joints" ~> "{" ~> rep(joint) <~ "}"

    def joint = quotedString ~ intNumber ~ vector3 ~ vector3 ^^ {
      case name ~ parent ~ pos ~ compressedQuat => {
        new Joint(name, parent, pos, new Quaternion(computeR(compressedQuat), compressedQuat.x, compressedQuat.y, compressedQuat.z))
      }
    }

    def vector3 = "(" ~> floatNumber ~ floatNumber ~ floatNumber <~ ")" ^^ {
      case x ~ y ~ z => {
        new Vector3(x,y,z)
      }
    }

    def meshes = rep(mesh)

    def mesh = "mesh" ~> "{" ~> shader ~ verts ~ tris ~ weights <~ "}" ^^ {
      case shader ~ verts ~ tris ~ weights => {
        new Mesh(shader, verts, tris, weights)
      }
    }

    def shader = "shader" ~> quotedString 

    def verts = "numverts" ~> natNumber ~ rep(vert) ^^ { case numVerts ~ verts => 
      checkNum("numVerts", numVerts, verts);
    } 

    def vert = "vert" ~> natNumber ~ "(" ~ floatNumber ~ floatNumber ~ ")" ~ intNumber ~ intNumber ^^ {
      case vertNum ~ "(" ~ u ~ v ~ ")" ~ firstWeight ~ numWeights => {
        new Vert(u, v, firstWeight, numWeights)
      }
    }

    def tris = "numtris" ~> natNumber ~ rep(tri) ^^ { case numTris ~ tris => 
      checkNum("numTris", numTris, tris);
    }
    def tri = "tri" ~> natNumber ~ intNumber ~ intNumber ~ intNumber ^^ {
      case triNum ~ i1 ~ i2 ~ i3 => {
        new Tri(List(i1,i2,i3).toArray)
      }
    }

    def weights = "numweights" ~> natNumber ~ rep(weight) ^^ { case numWeights ~ weights => 
      checkNum("numWeights", numWeights, weights) 
    }
    def weight = "weight" ~> natNumber ~ intNumber ~ floatNumber ~ vector3 ^^ {
      case weightNum ~ boneIndex ~ bias ~ w => {
        new Weight(boneIndex, bias, w)
      }
    }

    //just a shortcut
    def floatNumber = floatingPointNumber ^^ { _.toFloat }
    //int >= 0
    def natNumber = decimalNumber ^^ { _.toInt }
    //int
    def intNumber = wholeNumber ^^ { _.toInt }

    def quotedString = stringLiteral ^^ { str => 
      if (!(str.charAt(0) == '"' && str.charAt(str.length-1) == '"')) {
        throw new LoadingError("Expected quoted string : " + str)
      }
      str.substring(1, str.length-1)
    }
  }
}

// vim: set ts=2 sw=2 et:
