package md5viewer
import utils._
import scala.collection.mutable.MutableList
import java.nio._
import org.lwjgl.opengl.GL11._
import org.lwjgl._
import engine._
import org.newdawn.slick.opengl._
import java.io.FileInputStream
import java.io.IOException


//Model stuff
protected class Joint(val number: Int, val name: String, val parentIndex: Int, var position: Vector3, var rotation: Quaternion) {
  //Since position/rotation might get changed by animation, we save their original values here
  val originalPosition = position
  val originalRotation = rotation
  val children = new MutableList[Joint]
  var parentJoint : Joint = null
}

protected class Vert(val texCoordU : Float, val texCoordV : Float, val firstWeight: Int, val numWeights: Int) {
}

protected class Tri(val indices: Array[Int]) {
}

protected class Weight(val jointIndex: Int, val bias: Float, val w: Vector3) {
}


protected class Mesh(rawShader: String, val verts: List[Vert], val tris: List[Tri], val weights: List[Weight]) {
  val shader = "models/".r.replaceAllIn(rawShader, "data/textures/")
  val colorTex = try {
    TextureLoader.getTexture("TGA", new FileInputStream(shader+"_d.tga"))
  } catch {
    case ioe: IOException => null
  }

  val vertBuffer = BufferUtils.createFloatBuffer(verts.length*3)
  val indicesBuffer = createIndicesBuffer()
  val texCoordsBuffer = createTexCoordsBuffer()

  def createIndicesBuffer () : IntBuffer = {
    val buff = BufferUtils.createIntBuffer(tris.length*3)
    for (tri <- tris) {
      buff.put(tri.indices)
    }
    buff.rewind()
    buff
  }

  def createTexCoordsBuffer () : FloatBuffer = {
    val texCoordsBuff = BufferUtils.createFloatBuffer(verts.length*2)
    for (vert <- verts) {  
      texCoordsBuff.put(vert.texCoordU)
      texCoordsBuff.put(vert.texCoordV)
    }
    texCoordsBuff
  }

  def skin (joints: List[Joint]) {
    vertBuffer.rewind()

    for (i <- 0 until verts.length) {  
      var vertice = Vector3(0,0,0)

      val baseIdx = verts(i).firstWeight
      for (j <- 0 until verts(i).numWeights) { //for each joint this vert depends upon
        val joint = joints(weights(baseIdx+j).jointIndex)
        val wpos = joint.rotation.rotate(weights(baseIdx+j).w)
        val pos = (wpos + joint.position)*weights(baseIdx+j).bias
        vertice += pos
      }

      vertBuffer.put(vertice.x)
      vertBuffer.put(vertice.y)
      vertBuffer.put(vertice.z)
    }
    vertBuffer.rewind()
  }

  def draw () {
    if (colorTex != null) {
      glEnable(GL_TEXTURE_2D)
      colorTex.bind()
    } else {
      glDisable(GL_TEXTURE_2D)
    }
    vertBuffer.rewind()
    glVertexPointer(3, 0, vertBuffer)
    texCoordsBuffer.rewind()
    glTexCoordPointer(2, 0, texCoordsBuffer)
    glDrawElements(GL_TRIANGLES, indicesBuffer)
  }

}

//Model class
class MD5Model(val version: Int, val commandLine: String, val joints: List[Joint], val meshes: List[Mesh]) {
  val rotation = Quaternion(-MathUtils.PI_2, Vector3(1,0,0))*Quaternion(-MathUtils.PI_2, Vector3(0,0,1))
  val baseJoints = buildJointsHierarchy()

  for (m <- meshes)
    m.skin(joints)

  private def buildJointsHierarchy () : List[Joint] = {
    val baseJoints = new MutableList[Joint]
    for (joint <- joints) {
      if (joint.parentIndex < 0) {
        baseJoints += joint
      } else { //not a base joint => set its parent and add it to the children list
        joint.parentJoint = joints(joint.parentIndex)
        joint.parentJoint.children += joint
      }
    }
    baseJoints.toList
  }

  def skin () {
    for (m <- meshes) {
      m.skin(joints)
    }
  }

  def draw () {
    glPushMatrix()
    Renderer.applyRotation(rotation)
    //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    glColor4f(1,1,1,1)
    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_TEXTURE_COORD_ARRAY)
    for (m <- meshes) {
      m.draw()
    }
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_TEXTURE_COORD_ARRAY)
    glDisable(GL_TEXTURE_2D)
    //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    glPopMatrix()

  }
}

//Anim

protected class JointInfo (val parent: Int, val flags: Int, val frameIndex: Int) {
}

protected class JointBaseFrame (val position: Vector3, val rotation: Quaternion) {
}

protected class BaseFrame (val joints: List[JointBaseFrame]) {
}

protected class Frame (val values: Array[Float]) {
}

class MD5Anim (val version: Int, val commandLine: String, val jointInfos: List[JointInfo], val baseframe : BaseFrame, val frames: List[Frame], frameRate: Int) {
  var currentTime = 0.0f
  var previousTime = 0.0f

  var currentFrame = 0

  def animate (model: MD5Model, elapsedTime: Float) {
    currentTime += elapsedTime
    if ((currentTime-previousTime) > 1/frameRate.toFloat) {
      previousTime = currentTime
      currentFrame += 1
      if (currentFrame >= frames.length) {
        currentFrame = 0
      }
      for (joint <- model.baseJoints) {
        animateJoint(joint, currentFrame, Quaternion(0,Vector3(0,1,0)), Vector3(0,0,0))
      }
      model.skin()
    }
  }

  def animateJoint (joint: Joint, frame: Int, parentRotation: Quaternion, parentPosition: Vector3) {
    val animatedPosition = baseframe.joints(joint.number).position
    val animatedRotation = baseframe.joints(joint.number).rotation

    val flags = jointInfos(joint.number).flags
    val baseIdx = jointInfos(joint.number).frameIndex
    var n = 0
    if ((flags & 1) == 1) { //Tx
      animatedPosition.x = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 2) == 2) { //Ty
      animatedPosition.y = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 4) == 4) { //Tz
      animatedPosition.z = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 8) == 8) { //Qx
      animatedRotation.x = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 16) == 16) { //Qy
      animatedRotation.y = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 32) == 32) { //Qz
      animatedRotation.z = frames(frame).values(baseIdx+n)
      n += 1
    }
    animatedRotation.computeR()

    if (joint.parentJoint == null) { //base joint
      joint.position = animatedPosition
      joint.rotation = animatedRotation
    } else { //has a parent
      joint.position = parentRotation.rotate(animatedPosition) + parentPosition
      joint.rotation = parentRotation*animatedRotation
    }

    for (child <- joint.children) {
      animateJoint(child, frame, joint.rotation, joint.position)
    }
  }
}
// vim: set ts=2 sw=2 et:
