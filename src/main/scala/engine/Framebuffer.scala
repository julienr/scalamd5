package engine
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.ARBFramebufferObject._
import org.lwjgl.opengl.GL31._

//Expose the functionnality of an OpenGL framebuffer
//At least one attachment (color or depth) should be created
//TODO: Resize function ?
class Framebuffer (val width: Int, val height: Int) {
  var depthTex: Int = -1
  var colorTex: Int = -1
  private val fbo = glGenFramebuffers()

  override def finalize () {
    glDeleteFramebuffers(fbo)
  }

  def createColorAttachment () { 
    if (colorTex != -1) {
      Console.println("Color attachment already exists")
      return
    }
    colorTex = genRectTexture(GL_RGBA)

    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_RECTANGLE, colorTex, 0)
    checkStatus("color attachment creation")
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
  }

  def createDepthAttachment () {
    if (depthTex != -1) {
      Console.println("Depth attachment already exists")
    }
    depthTex = genRectTexture(GL_DEPTH_COMPONENT)

    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_RECTANGLE, depthTex, 0)
    checkStatus("depth attachment creation")
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
  }

  //Save and clear the current modelview/projection matrices, clear the framebuffer and bind it for rendering
  def startCapturing () {
    Renderer.saveMatrices()
    Renderer.saveViewport()
    glViewport(0,0, width, height)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    bind()
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
  }

  def stopCapturing () {
    unbind()
    Renderer.restoreViewport()
    Renderer.restoreMatrices()
  }

  def bind() {
    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
  }

  def unbind() {
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
  }

  private def checkStatus (diagnostic: String) {
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE) {
      Console.println("Error initializing framebuffer")
      Renderer.checkGLError(diagnostic)
    }
  }

  private def genRectTexture(pixFormat: Int): Int = {
    val id = glGenTextures()
    glBindTexture(GL_TEXTURE_RECTANGLE, id)

    //TODO: Use GL_LINEAR for PCF ?
    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    // Remove artefact on the edges of the shadowmap
    glTexParameterf(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameterf(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, GL_CLAMP);

    //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE)
    glTexImage2D(GL_TEXTURE_RECTANGLE, 0, pixFormat, 
                 width, height, 0, pixFormat, GL_UNSIGNED_BYTE, 
                 null.asInstanceOf[java.nio.IntBuffer])
    return id
  }
}

// vim: set ts=2 sw=2 et:
