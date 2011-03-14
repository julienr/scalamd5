package test.scala.md5viewer
import md5viewer._
import org.scalatest._
import org.scalatest.matchers._
import utils._

class MD5ParserCombinatorsSpec extends WordSpec with ShouldMatchers {
  "MD5ParserCombinators" should {
    "correctly remove comments" in {
      val input = 
        """
        line 1
        line 2 // blahf comment
        line 3
        """
      val res = MD5Loader.removeComments(input)
      res should equal(
        """
        line 1
        line 2 
        line 3
        """)
    }

    "parse a minimal file" in {
      val input = """
        MD5Version 10
        commandline "blah"
        numJoints 1
        numMeshes 1
        joints {
          "origin" -1 ( 0 0 0 ) ( -0.5 -0.5 -0.5 ) // none
        }

        mesh {
          //my mesh
          shader "blahShader"

          numverts 1
          vert 0 ( 0.5 0.5 ) 1 4

          numtris 1
          tri 1 2 3 4

          numweights 1
          weight 1 2 3.3 ( 0.5 0.3 0.7 )
        }
        """
      val p = new MD5Loader.MD5ParserCombinators
      p.parseAll(p.model, MD5Loader.removeComments(input)) match {
        case p.Success(m,_) => {
          m.version should equal(10)
          m.commandLine should equal("blah")
          m.joints.length should equal(1)
          m.meshes.length should equal(1)
          //Joint
          val j = m.joints(0)
          j.name should equal("origin")
          j.parentIndex should equal(-1)
          assert(j.position ~= Vector3(0,0,0))
          j.rotation.x should equal(-0.5)
          j.rotation.y should equal(-0.5)
          j.rotation.z should equal(-0.5)

          //Mesh
          val mesh = m.meshes(0)
          mesh.shader should equal("blahShader")

          mesh.verts.length should equal(1)
          val v = mesh.verts(0)
          v.texCoordU should equal(0.5)
          v.texCoordV should equal(0.5)
          v.firstWeight should equal(1)
          v.numWeights should equal(4)

          mesh.tris.length should equal(1)
          mesh.tris(0).indices should equal(List(2,3,4).toArray)

          mesh.weights.length should equal(1)
          val w = mesh.weights(0)
          w.boneIndex should equal(2)
          w.bias should equal(3.3f)
          assert(w.w ~= Vector3(0.5f,0.3f,0.7f))
        }

        //Console.println(r) /*r.toString should equal("""(((((10~"blah")~1)~1)~List(((("origin"~-1)~((0~0)~0))~((-0.5~-0.5)~-0.5))))~List(((("blahShader"~(1~List(0)))~(1~List((((1~2)~3)~4))))~(1~List((((1~2)~3)~((0.5~0.3)~0.7)))))))""")*/
        case x => fail(x.toString)
      }
    }
  }
}

// vim: set ts=2 sw=2 et:
