package md5viewer
import utils._
import scala.collection.mutable.MutableList
import java.nio._
import org.lwjgl.opengl.GL11._
import org.lwjgl._
import engine._

protected class Joint(val name: String, val parentIndex: Int, val position: Vector3, val rotation: Quaternion) {
  val children = new MutableList[Joint]
  var parentJoint : Joint = null
}

protected class Vert(val texCoordU : Float, val texCoordV : Float, val firstWeight: Int, val numWeights: Int) {
}

protected class Tri(val indices: Array[Int]) {
}

protected class Weight(val jointIndex: Int, val bias: Float, val w: Vector3) {
}


protected class Mesh(val shader: String, val verts: List[Vert], val tris: List[Tri], val weights: List[Weight]) {

}

class MD5Model(val version: Int, val commandLine: String, val joints: List[Joint], val meshes: List[Mesh]) {
  val rotation = Quaternion(-MathUtils.PI_2, Vector3(1,0,0))*Quaternion(-MathUtils.PI_2, Vector3(0,0,1))
  buildJointsHierarchy()
  val vertBuffer = skinMesh(meshes(0))
  val indicesBuffer = indicesToBuffer(meshes(0))

  private def buildJointsHierarchy () {
    val baseJoints = new MutableList[Joint]
    for (joint <- joints) {
      if (joint.parentIndex < 0) {
        baseJoints += joint
      } else { //not a base joint => set its parent and add it to the children list
        joint.parentJoint = joints(joint.parentIndex)
        joint.parentJoint.children += joint
      }
    }
  }

  def indicesToBuffer (m: Mesh) : IntBuffer = {
    val buff = BufferUtils.createIntBuffer(m.tris.length*3)
    for (tri <- m.tris) {
      buff.put(tri.indices)
    }
    buff.rewind()
    buff
  }

  def skinMesh (m: Mesh) : FloatBuffer = {
    val buff = BufferUtils.createFloatBuffer(m.verts.length*3)

    for (i <- 0 until m.verts.length) {  
      var vertice = Vector3(0,0,0)

      val baseIdx = m.verts(i).firstWeight
      for (j <- 0 until m.verts(i).numWeights) { //for each joint this vert depends upon
        val joint = joints(m.weights(baseIdx+j).jointIndex)
        val wpos = joint.rotation.rotate(m.weights(baseIdx+j).w)
        val pos = (wpos + joint.position)*m.weights(baseIdx+j).bias
        vertice += pos
      }

      buff.put(vertice.x)
      buff.put(vertice.y)
      buff.put(vertice.z)
//      Console.printf("vert (%d) = %s\n", i, vertice)
    }
    buff.rewind()
    buff
  }

  def draw () {
    glPushMatrix()
    Renderer.applyRotation(rotation)
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    glPointSize(4.0f)
    glColor4f(1,1,1,1)
    glEnableClientState(GL_VERTEX_ARRAY)
    vertBuffer.rewind()
    glVertexPointer(3, 0, vertBuffer)
    //glDrawArrays(GL_POINTS, 0, meshes(0).verts.length)
    glDrawElements(GL_TRIANGLES, indicesBuffer)
    glDisableClientState(GL_VERTEX_ARRAY)
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    glPopMatrix()
  }
}


// vim: set ts=2 sw=2 et:
