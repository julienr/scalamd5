package md5viewer
import utils._
import scala.collection.mutable.MutableList
import java.nio._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
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
  var normal = Vector3(0,0,0)
  var tangent = Vector3(0,0,0)

  def calculateNormalTangent (verts: Array[Vector3], texCoords: Array[Float]) {
    val v0 = verts(indices(0))
    val v1 = verts(indices(2))
    val v2 = verts(indices(1))

    val side0 = v0-v1
    val side1 = v2-v1

    //normal
    normal = side1%side0
    normal.normalize()
    
    def texU (idx: Int) = texCoords(2*indices(idx))
    //tangent (actually, this is U tangent, we don't compute V tangent because it is normal%tangent)
    val deltaU0 = texU(0) - texU(1)
    val deltaU1 = texU(2) - texU(1)
    tangent = side0*deltaU1 - side1*deltaU0
    tangent.normalize()
  }
}

protected class Weight(val jointIndex: Int, val bias: Float, val w: Vector3) {
  var normal = Vector3(0,0,0)
  var tangent = Vector3(0,0,0)
}


protected class Mesh(rawShader: String, val verts: List[Vert], val tris: List[Tri], val weights: List[Weight]) {
  val shader = "models/".r.replaceAllIn(rawShader, "data/textures/")
  val colorTex = loadTex(shader+"_d.tga", GL_LINEAR)
  val localTex = loadTex(shader+"_local.tga", GL_LINEAR)
  val specularTex = loadTex(shader+"_s.tga", GL_LINEAR)

  val glmesh = new GLMesh(verts.length, tris.length)
  glmesh.addTex(GL_TEXTURE0, colorTex, "colorTex", 0)
  glmesh.addTex(GL_TEXTURE1, localTex, "localTex", 1)
  glmesh.addTex(GL_TEXTURE2, specularTex, "specularTex", 2)

  fillIndicesBuffer()
  fillTexCoordsBuffer()

  def loadTex (file: String, filter: Int) = {
    try {
      Console.println("loading : " + file)
      TextureLoader.getTexture("TGA", new FileInputStream(file), filter)
    } catch {
      case ioe: IOException => Console.println(ioe); null
    }
  }

  def fillIndicesBuffer () {
    val buff = glmesh.indicesBuffer
    buff.rewind()
    for (tri <- tris) {
      buff.put(tri.indices)
    }
  }

  def fillTexCoordsBuffer () {
    val texCoordsBuff = glmesh.texCoordsBuffer
    for (vert <- verts) {  
      texCoordsBuff.put(vert.texCoordU)
      texCoordsBuff.put(vert.texCoordV)
    }
  }

  //Calculate initial skin. Called once just after model loading.
  //Mostly used to calculate by-weight normals that can be reused later
  def initialSkin (joints: List[Joint]) {
    val vertPos = new Array[Vector3](verts.length)
    val texCoords = new Array[Float](verts.length*2)

    val vertBuffer = glmesh.vertBuffer

    //Calculate initial vertex position
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
      vertPos(i) = vertice
      texCoords(2*i) = verts(i).texCoordU
      texCoords(2*i+1) = verts(i).texCoordV
    }
    vertBuffer.rewind()

    //Calculate per-triangle normals and tangents
    //Also sum per-vertex normals and tangents. For a given vertex,
    //its normal is the average of the normals of all triangle the vertex belongs to
    val vertNormals = new Array[Vector3](verts.length)
    val vertTangents = new Array[Vector3](verts.length)
    for (i <- 0 until verts.length) {
      vertNormals(i) = Vector3(0,0,0)
      vertTangents(i) = Vector3(0,0,0)
    }
    //sum per-vertex normal, tangent
    for (tri <- tris) {
      tri.calculateNormalTangent(vertPos, texCoords)
      for (i <- 0 until 3) {
        vertNormals(tri.indices(i)) += tri.normal
        vertTangents(tri.indices(i)) += tri.tangent
      }
    }
    //normalize per-vertex normal, tangent
    for (i <- 0 until verts.length) {
      vertNormals(i).normalize()
      vertTangents(i).normalize()
    }

    //Since, when animating, the vertices changes, we would have to recalculate these normals after
    //each animation step. To avoid that, we will store them in the weights. First, we need to transform
    //them to joint space so they are animation-invariant. 
    //As before, the normal of a weight is simply the average of the normals of all vertices that are attached
    //to it
    for (i <- 0 until verts.length) {
      val baseIdx = verts(i).firstWeight
      for (j <- 0 until verts(i).numWeights) { 
        val weight = weights(baseIdx+j)
        val joint = joints(weight.jointIndex)
        val invJointRot = joint.rotation.getInverse
        weight.normal += invJointRot.rotate(vertNormals(i))
        weight.tangent += invJointRot.rotate(vertTangents(i))
      }
    }

    for (w <- weights) {
      w.normal.normalize()
      w.tangent.normalize()
    }
  }

  def skin (joints: List[Joint]) {
    def addToBuff (buff: FloatBuffer, v: Vector3) {
      buff.put(v.x)
      buff.put(v.y)
      buff.put(v.z)
    }
    val vertBuffer = glmesh.vertBuffer
    val normalBuffer = glmesh.normalBuffer
    val tangentBuffer = glmesh.getAttribBuffer("tangent")
    vertBuffer.rewind()
    normalBuffer.rewind()
    tangentBuffer.rewind()

    for (i <- 0 until verts.length) {  
      var position = Vector3(0,0,0)
      var normal = Vector3(0,0,0)
      var tangent = Vector3(0,0,0)

      val baseIdx = verts(i).firstWeight
      for (j <- 0 until verts(i).numWeights) { //for each weight this vert depends upon
        val weight = weights(baseIdx+j)
        val joint = joints(weight.jointIndex)
        val wpos = joint.rotation.rotate(weight.w)
        val pos = (wpos + joint.position)*weight.bias
        position += pos
        normal += joint.rotation.rotate(weight.normal)
        tangent += joint.rotation.rotate(weight.tangent)
      }

      addToBuff(vertBuffer, position)
      
      normal.normalize()
      addToBuff(normalBuffer, normal)

      tangent.normalize()
      addToBuff(tangentBuffer, tangent)

    }
    vertBuffer.rewind()
    normalBuffer.rewind()
    tangentBuffer.rewind()
  }
}

//Model class
class MD5Model(val version: Int, val commandLine: String, val joints: List[Joint], val meshes: List[Mesh]) {
  val glentity = new GLEntity 
  glentity.rotation = Quaternion(-MathUtils.PI_2, Vector3(1,0,0))*Quaternion(-MathUtils.PI_2, Vector3(0,0,1))

  val baseJoints = buildJointsHierarchy()

  //We could call initialSkin directly in Mesh' constructor, but this would then happens during parsing... 
  for (m <- meshes) {
    m.initialSkin(joints)
    m.skin(joints)
    glentity.addMesh(m.glmesh)
  }



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
}


// vim: set ts=2 sw=2 et:
