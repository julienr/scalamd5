package engine
import utils._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.ARBFramebufferObject._

//Type of attachments supported by a framebuffer
object Attachment extends Enumeration {
  type Attachment = Value
  val Depth, Color = Value
}

//Expose the functionnality of an OpenGL framebuffer
//Also specify the list of attachment types that should be created
//TODO: Resize function ?
class Framebuffer (val width: Int, val height: Int, att: List[Attachment.Attachment]) {
  import Attachment._

  private val fbo = glGenFramebuffers()
  glBindFramebuffer(GL_FRAMEBUFFER, fbo)

  //We can disable draw buffer if we only have depth attachment
  val hasColor = att.contains(Color)
  if (!hasColor) {
    glDrawBuffer(GL_NONE)
    glReadBuffer(GL_NONE)
  }

  //map attachments types to their opengl tex id
  private val attachments: Map[Attachment, Int] = {
     att.map((a: Attachment) => a match {
        case Depth => (a, createDepthAttachment())
        case Color => (a, createColorAttachment())
    }).toMap[Attachment, Int]
  }
  checkStatus("FBO initialization")
  glBindFramebuffer(GL_FRAMEBUFFER, 0)

  override def finalize () {
    glDeleteFramebuffers(fbo)
  }

  private def createColorAttachment () : Int = { 
    val colorTex = genRectTexture(GL_RGBA, GL_RGBA)

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTex, 0)
    return colorTex
  }

  private def createDepthAttachment () : Int = {
    val depthTex = genRectTexture(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT)

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTex, 0)
    return depthTex
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

    glBindFramebuffer(GL_FRAMEBUFFER, fbo)
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
  }

  def stopCapturing () {
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    Renderer.restoreViewport()
    Renderer.restoreMatrices()
  }

  //Bind the color/depth texture
  def bindAttachmentTex(att: Attachment) {
    if (!attachments.contains(att)) {
      glDisable(GL_TEXTURE_2D) 
    } else {
      glEnable(GL_TEXTURE_2D)
      glBindTexture(GL_TEXTURE_2D, attachments(att))
    }
  }

  def unbindTex () {
    glDisable(GL_TEXTURE_2D)
  }

  //Will draw the given attachment to a square on the screen (ASSUMES ortho projection has been set up)
  def drawToRect (att: Attachment, rect: Rectangle) {
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_TEXTURE_COORD_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glActiveTexture(GL_TEXTURE0)
    bindAttachmentTex(att)
    
    glColor4f(1,1,1,1)
    glBegin(GL_QUADS)
    //This would be useful if we used GL_TEXTURE_RECTANGLE
    /*      glVertex2i(540,380)
    glTexCoord2f(shadowFBO.width,shadowFBO.height)

    glVertex2i(640,380)
    glTexCoord2f(shadowFBO.width,0)

    glVertex2i(640,480)
    glTexCoord2f(0,0)

    glVertex2i(540,480)
    glTexCoord2f(0,shadowFBO.height)*/
    glVertex2f(rect.xMin, rect.yMin)
    glTexCoord2f(1.0f,1.0f)

    glVertex2f(rect.xMax, rect.yMin)
    glTexCoord2f(1.0f,0)

    glVertex2f(rect.xMax, rect.yMax)
    glTexCoord2f(0,0)

    glVertex2f(rect.xMin, rect.yMax)
    glTexCoord2f(0,1.0f)

    glEnd()
    unbindTex()
  }


  private def checkStatus (diagnostic: String) {
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE) {
      Console.println("Error initializing framebuffer")
      Renderer.checkGLError(diagnostic)
    }
  }

  private def genRectTexture(intFormat: Int, pixFormat: Int): Int = {
    val id = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, id)

    glTexImage2D(GL_TEXTURE_2D, 0, intFormat, 
                 width, height, 0, pixFormat, GL_UNSIGNED_INT, 
                 null.asInstanceOf[java.nio.IntBuffer])

    //TODO: Use GL_LINEAR for PCF ?
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    // Remove artefact on the edges of the shadowmap
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);

    //TODO: Should we keep it ?
    //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE)
	
    glBindTexture(GL_TEXTURE_2D, 0);

    return id
  }
}

// vim: set ts=2 sw=2 et:
