package engine
import collection.mutable.HashMap
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
import collection.mutable.HashSet

//An entity is a collection of meshes that share the same rotation/position transform
class GLEntity {
  var position = Vector3(0,0,0)
  var rotation = Quaternion(0, Vector3(0,1,0))
  var scale = Vector3(1,1,1)

  private val meshes = new HashSet[GLMesh]

  def addMesh (m: GLMesh) = meshes += m

  def draw (glProgram: GLSLProgram) {
    glPushMatrix()
    glTranslatef(position.x, position.y, position.z)
    Renderer.applyRotation(rotation)
    glScalef(scale.x, scale.y, scale.z)

   //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    glColor4f(1,1,1,1)
    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_NORMAL_ARRAY)
    glEnableClientState(GL_TEXTURE_COORD_ARRAY)
    for (m <- meshes) {
      m.draw(glProgram)
    }
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glDisableClientState(GL_TEXTURE_COORD_ARRAY)
    //glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

    glPopMatrix()
  }

  def drawNormals () {
    glPushMatrix()
    Renderer.applyRotation(rotation)
    for (m <- meshes) {
      m.drawNormals()
    }
    glPopMatrix()
  }
}

//A mesh is simply a collection of buffers that will be draw
//It originally consist of set of buffers for vertices, normals, tex coords and indices
//Additionnal attribute buffers can be attached
class GLMesh (numVerts: Int, numTris: Int) {
  val vertBuffer = BufferUtils.createFloatBuffer(numVerts*3)
  val normalBuffer = BufferUtils.createFloatBuffer(numVerts*3)
  val indicesBuffer = BufferUtils.createIntBuffer(numTris*3)

  //We don't need to bind one tex coord array per texture, since they all have the same coords
  //The shader will take care of using the first tex coord array for all of them
  val texCoordsBuffer = BufferUtils.createFloatBuffer(numVerts*2)

  //map OpenGL channel to texture and the name of the corresponding GLSL programm uniform sampler
  class TexInfo (val tex: Texture, val samplerName: String, val samplerUnit: Int)
  private val textures = new HashMap[Int, TexInfo]

  def addTex (channel: Int, tex: Texture, samplerName: String, samplerUnit: Int) { textures.put(channel, new TexInfo(tex,samplerName, samplerUnit)) }

  private val attributeBuffers = new HashMap[String, FloatBuffer]

  def getAttribBuffer(attribName: String) = attributeBuffers.getOrElseUpdate(attribName, BufferUtils.createFloatBuffer(numVerts*3))

  def draw (glProgram: GLSLProgram) {
    //Bind textures
    if (glProgram != null) {
      for ((channel, texInfo) <- textures) {
        bindTex(channel, texInfo.tex)
        glProgram.setSamplerUnit(texInfo.samplerName, texInfo.samplerUnit)
      }
    }

    vertBuffer.rewind()
    glVertexPointer(3, 0, vertBuffer)
    texCoordsBuffer.rewind()

    glClientActiveTexture(GL_TEXTURE0)
    glTexCoordPointer(2, 0, texCoordsBuffer)
    normalBuffer.rewind()
    glNormalPointer(0, normalBuffer)

    //Bind all supplementary attrib buffers
    if (glProgram != null) {
      for ((attribName, buffer) <- attributeBuffers) {
        buffer.rewind()
        glProgram.setAttribPointer(attribName, 3, false, buffer)
      }
      Renderer.checkGLError("attrib pointers")
    }
    indicesBuffer.rewind()
    glDrawElements(GL_TRIANGLES, indicesBuffer)

    //unbind tex
    if (glProgram != null) {
      for ((channel, texInfo) <- textures)
        unbindTex(channel, texInfo.tex)
    }
  }

  def drawNormals () {
    glActiveTexture(GL_TEXTURE0)
    glDisable(GL_TEXTURE_2D)
    glActiveTexture(GL_TEXTURE1)
    glDisable(GL_TEXTURE_2D)

    //TODO: We ASSUME we have a 'tangent' attrib
    val tangentBuffer = getAttribBuffer("tangent")

    vertBuffer.rewind()
    normalBuffer.rewind()
    tangentBuffer.rewind()
    
    def drawLine (start: (FloatBuffer, Int), dir: (FloatBuffer, Int)) {
      def v (b: FloatBuffer, i: Int) = Vector3(b.get(i), b.get(i+1), b.get(i+2))
      def glv (v: Vector3) { glVertex3f(v.x, v.y, v.z) }
      val startV = v(start._1, start._2)
      glv(startV)
      val dirV = v(dir._1, dir._2)
      glv(startV+dirV)
    }

    glBegin(GL_LINES)
    for (i <- Range(0,numVerts*3,3)) {
      glColor4f(1,0,0,1)
      drawLine((vertBuffer,i), (normalBuffer,i))
      glColor4f(0,1,0,1)
      drawLine((vertBuffer,i), (tangentBuffer,i))
    }
    glEnd()
  }


  def bindTex (unit: Int, texture: Texture) {
    glActiveTexture(unit)
    if (texture != null) {
      glEnable(GL_TEXTURE_2D)
      //glBindTexture(GL_TEXTURE_2D, texture.getTextureID())
      texture.bind()
    } else {
      glDisable(GL_TEXTURE_2D)
    }
  }

  def unbindTex (unit: Int, texture: Texture) {
    glActiveTexture(unit)
    glDisable(GL_TEXTURE_2D)

  }
}

// vim: set ts=2 sw=2 et:
