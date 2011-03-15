package md5viewer
import utils._
import scala.util.parsing.combinator._
import scala.util.matching.Regex
import java.io._
import collection.immutable.HashMap

object MD5Loader {
  class LoadingError(cause: String) extends RuntimeException(cause.toString)

  def removeComments (text: String) : String = {
    val regexp = """(?m)//.*$""".r
    return regexp.replaceAllIn(text, "")
  }

  //Helper function, checks that expected == loaded.length and throw exception if not. Returns loaded on success
  def checkNum[T] (what: String, expected: Int, loaded: List[T]) : List[T] = {
    if (expected != loaded.length) {
      throw new LoadingError("Inconsistent "+what+"("+expected+") and loaded number: " + loaded.length)
    }
    loaded
  }

  //Will load the first .md5mesh file from the given directory
  def loadFromDirectory (dir: String) : MD5Model = {
    val meshFiles = new File(dir).listFiles(new FilenameFilter() {
      def accept (dir: File, name: String) = name.endsWith(".md5mesh")
    })
    if (meshFiles.length == 0)
      throw new LoadingError("No .md5mesh found in [" + dir+"]")

    val lines = io.Source.fromFile(meshFiles(0)).mkString
    val p = new MD5ModelParser
    val model = p.parseAll(p.model, MD5Loader.removeComments(lines)) match {
      case p.Success(m,_) => m
      case x => throw new Exception("Error loading : " + x)
    }
    model
  }

  def loadAnim (file: String) : MD5Anim = {
    val lines = io.Source.fromFile(file).mkString
    val p = new MD5AnimParser
    val anim = p.parseAll(p.anim, MD5Loader.removeComments(lines)) match {
      case p.Success(a,_) => a
      case x => throw new Exception("Error loading : " + x)
    }
    anim
  }

  class MD5GenericParser extends JavaTokenParsers {
    def version = "MD5Version" ~> natNumber

    def commandline = "commandline" ~> quotedString

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

    def vector3 = "(" ~> floatNumber ~ floatNumber ~ floatNumber <~ ")" ^^ {
      case x ~ y ~ z => {
        new Vector3(x,y,z)
      }
    }
  }


  //A parser for MD5 model files. Calling parseAll(model, ...) will return a Model
  class MD5ModelParser extends MD5GenericParser {
    def model = version ~ commandline ~ numJoints ~ numMeshes ~ joints ~ meshes ^^ {
      case v ~ cl ~ numJoints ~ numMeshes ~ joints ~ meshes => {
        new MD5Model(v, cl, checkNum("numJoints", numJoints, joints), checkNum("numMeshes", numMeshes, meshes))
      }
    }
 
    def numJoints = "numJoints" ~> natNumber

    def numMeshes = "numMeshes" ~> natNumber

    def joints = "joints" ~> "{" ~> rep(joint) <~ "}"

    var jointCnt = 0
    def joint = quotedString ~ intNumber ~ vector3 ~ vector3 ^^ {
      case name ~ parent ~ pos ~ compressedQuat => {
        val r = new Joint(jointCnt, name, parent, pos, new Quaternion(compressedQuat.x, compressedQuat.y, compressedQuat.z))
        jointCnt += 1
        r
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
  }


  class MD5AnimParser extends MD5GenericParser {
    def anim = version ~ commandline ~ numFrames ~ numJoints ~ frameRate ~ numAnimatedComponents ~ hierarchy ~ bounds ~ baseframe ~ rep(frame) ^^ {
      case version ~ commandline ~ numFrames ~ numJoints ~ frameRate ~ numAnimatedComponents ~ hierarchy ~ bounds ~ baseFrame ~ frames => {
        new MD5Anim(version, commandline, hierarchy, baseFrame, frames, frameRate)
      }
    }

    def numFrames = "numFrames" ~> natNumber
    def numJoints = "numJoints" ~> natNumber
    def frameRate = "frameRate" ~> natNumber
    def numAnimatedComponents = "numAnimatedComponents" ~> natNumber

    def hierarchy = "hierarchy" ~> "{" ~> rep(jointInfo) <~ "}"  
    def jointInfo = quotedString ~ intNumber ~ intNumber ~ intNumber ^^ {
      case jointName ~ parent ~ flags ~ frameIndex => new JointInfo(parent, flags, frameIndex)
    }

    def bounds = "bounds" ~> "{" ~> rep(bound) <~ "}"
    def bound = vector3 ~ vector3 

    def baseframe = "baseframe" ~> "{" ~> rep(bframe) <~ "}" ^^ { 
      new BaseFrame(_)
    }
    def bframe = vector3 ~ vector3 ^^ {
      case pos ~ rot => new JointBaseFrame(pos, new Quaternion(rot.x, rot.y, rot.z))
    }

    def frame = "frame" ~> natNumber ~ "{" ~ rep(floatNumber) <~ "}" ^^ { case frameNum ~ "{" ~ comps => new Frame(comps.toArray) }
  }
}

// vim: set ts=2 sw=2 et:
