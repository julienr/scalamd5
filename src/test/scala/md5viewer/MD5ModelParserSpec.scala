package test.scala.md5viewer
import md5viewer._
import org.scalatest._
import org.scalatest.matchers._
import utils._

class MD5ModelParserSpec extends WordSpec with ShouldMatchers {
  "MD5ModelParser" should {
    "parse a minimal file" in {
      val input = """
        MD5Version 10
        commandline "blah"
        numJoints 1
        numMeshes 1
        joints {
          "origin" -1 ( 0.2 0.3 0.4 ) ( -0.5 -0.5 -0.5 ) // none
        }

        mesh {
          //my mesh
          shader "blahShader"

          numverts 3
          vert 0 ( 0.5 0.5 ) 0 1
          vert 1 ( -0.5 -0.5 ) 0 1
          vert 2 ( 1.5 1.5 ) 0 1

          numtris 1
          tri 0 0 1 2

          numweights 1
          weight 0 0 3.3 ( 0.5 0.3 0.7 )
        }
        """
      val p = new MD5Loader.MD5ModelParser
      p.parse(input) match {
        case p.Success(m,_) => {
          m.version should equal(10)
          m.commandLine should equal("blah")
          m.joints.length should equal(1)
          m.meshes.length should equal(1)
          //Joint
          val j = m.joints(0)
          j.name should equal("origin")
          j.parentIndex should equal(-1)
          assert(j.position ~= Vector3(0.2f,0.3f,0.4f))
          j.rotation.x should equal(-0.5)
          j.rotation.y should equal(-0.5)
          j.rotation.z should equal(-0.5)

          //Mesh
          val mesh = m.meshes(0)
          mesh.shader should equal("blahShader")

          mesh.verts.length should equal(3)
          val v = mesh.verts(0)
          v.texCoordU should equal(0.5)
          v.texCoordV should equal(0.5)
          v.firstWeight should equal(0)
          v.numWeights should equal(1)

          mesh.tris.length should equal(1)
          mesh.tris(0).indices should equal(List(0,1,2).toArray)

          mesh.weights.length should equal(1)
          val w = mesh.weights(0)
          w.jointIndex should equal(0)
          w.bias should equal(3.3f)
          assert(w.w ~= Vector3(0.5f,0.3f,0.7f))
        }
        case x => fail(x.toString)
      }
    }
  }
}

class MD5AnimParserSpec extends WordSpec with ShouldMatchers {
  "MD5AnimParser" should {
    "parse a minimal file" in {
      val input = """
      MD5Version 10
      commandline "anim"
      numFrames 1
      numJoints 1
      frameRate 24
      numAnimatedComponents 1

      hierarchy {
        "origin" -1 7 0
      }

      bounds {
        ( -1 -1 -1 ) ( 1 1 1 )
      }

      baseframe {
        ( 0 0 0 ) ( -0.5 -0.5 -0.5 )
      }

      frame 0 {
        0 0 0
      }
      """
      val p = new MD5Loader.MD5AnimParser
      val anim = p.parse(input) match {
        case p.Success(a,_) => a
        case x => fail(x.toString)
      }

      anim.version should equal(10)
      anim.jointInfos(0).parent should equal(-1)
      anim.jointInfos(0).flags should equal(7)
      anim.jointInfos(0).frameIndex should equal(0)
    }
  }
}

// vim: set ts=2 sw=2 et:
