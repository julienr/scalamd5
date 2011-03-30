package md5viewer
import engine._
import utils._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl._

//A xz-aligned quad
class Floor (size: Float) {
  val glmesh = new GLMesh(4, 4)
  initMesh()
  val glentity = new GLEntity()
  glentity.addMesh(glmesh)
  glentity.scale = Vector3(size,size,size)

  private def initMesh () {
    val vertBuffer = glmesh.vertBuffer
    val texCoordsBuff = glmesh.texCoordsBuffer
    val tangentBuffer = glmesh.getAttribBuffer("tangent")
    def p(x: Float, y:Float, z:Float) {
      vertBuffer.put(x)
      vertBuffer.put(y)
      vertBuffer.put(z)
      tangentBuffer.put(1.0f)
      tangentBuffer.put(0)
      tangentBuffer.put(0)
    }

    def t(u: Float, v: Float) {
      texCoordsBuff.put(u)
      texCoordsBuff.put(v)
    }

    p(-0.5f,0,-0.5f)
    t(0,0)
    p(0.5f,0,-0.5f)
    t(1,0)
    p(0.5f,0,0.5f)
    t(1,1)
    p(-0.5f,0,0.5f)
    t(0,1)

    
    val idx = glmesh.indicesBuffer
    idx.put(0)
    idx.put(1)
    idx.put(2)
    idx.put(0)
    idx.put(2)
    idx.put(3)

    vertBuffer.rewind()
    texCoordsBuff.rewind()
    tangentBuffer.rewind()
    idx.rewind()

    //Add some dummy textures
    glmesh.addTex(GL_TEXTURE0, TexUtils.loadWhiteTex(), "colorTex", 0)
    glmesh.addTex(GL_TEXTURE1, TexUtils.createMonoTexture(0,0,255), "localTex", 1)
    glmesh.addTex(GL_TEXTURE2, TexUtils.loadBlackTex(), "specularTex", 2)
  }
}

// vim: set ts=2 sw=2 et:
