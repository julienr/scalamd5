package engine
import utils._
import org.lwjgl.opengl._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.ARBFramebufferObject._
import org.lwjgl.util.glu._
import scala.collection.mutable.HashSet

object Renderer {
  var aspectRatio = 0.0f 

  val cameras = new HashSet[Camera]()

  var fbo: Framebuffer = null

  def colorTextureId : Int = fbo.colorTex
  def depthTextureId : Int = fbo.depthTex

  def shadowMapWidth = fbo.width
  def shadowMapHeight = fbo.height

  def initialize (w: Int, h: Int) {
    //initialization code
    GL11.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glCullFace(GL11.GL_BACK)
    GL11.glFrontFace(GL11.GL_CW)
/*    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)*/
    resizeWindow(w,h)
  }

  import org.lwjgl.opengl.GL31._
  private def initializeFBO (screenWidth: Int, screenHeight: Int) {
    /*val shadowMapRatio = 1
    shadowMapWidth = screenWidth*shadowMapRatio
    shadowMapHeight = screenHeight*shadowMapRatio
    if (depthTextureId == -1) { //first time initialization
      Console.println("First time FBO initialization")
      //FBO depth tex
      glEnable(GL_TEXTURE_RECTANGLE)
      depthTextureId = glGenTextures()
      glBindTexture(GL_TEXTURE_RECTANGLE, depthTextureId)
      //FBO color tex
      colorTextureId = glGenTextures()

      //FBO
      fboId = glGenFramebuffers();
    }

    Console.println("shadow width/height : ("+shadowMapWidth+", "+shadowMapHeight+")")

    glBindTexture(GL_TEXTURE_RECTANGLE, depthTextureId)
	  glTexImage2D( GL_TEXTURE_RECTANGLE, 0, GL_DEPTH_COMPONENT, shadowMapWidth, shadowMapHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, null.asInstanceOf[java.nio.IntBuffer]);


    glBindTexture(GL_TEXTURE_RECTANGLE, colorTextureId)
    glTexImage2D(GL_TEXTURE_RECTANGLE, 0, GL_RGBA, shadowMapWidth, shadowMapHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null.asInstanceOf[java.nio.IntBuffer])

	  glBindTexture(GL_TEXTURE_RECTANGLE, 0);

    glBindFramebuffer(GL_FRAMEBUFFER, fboId)


    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_RECTANGLE, depthTextureId, 0)

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_RECTANGLE, colorTextureId, 0)
    val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
    if (status != GL_FRAMEBUFFER_COMPLETE)
      Console.println("Error initializing framebuffer")

    //bind window-system framebuffer
    glBindFramebuffer(GL_FRAMEBUFFER, 0)
    checkGLError("Framebuffer initialization")
    glEnable(GL_TEXTURE_2D)*/
    if (fbo == null) {
     fbo = new Framebuffer(screenWidth, screenHeight)
      fbo.createColorAttachment()
      fbo.createDepthAttachment()
    }
  }

  def bindFBO () { 
    //TODO: Is it necessary to set viewport if shadow map width != window width
    //glViewport(0, 0, shadowMapWidth, shadowMapHeight)
    //glBindFramebuffer(GL_FRAMEBUFFER, fboId)
    fbo.startCapturing()
  }
  def unbindFBO() {
    fbo.stopCapturing()
    //= glBindFramebuffer(GL_FRAMEBUFFER, 0)
  }

  def saveMatrices () {
    glMatrixMode(GL_PROJECTION)
    glPushMatrix()
    glMatrixMode(GL_MODELVIEW)
    glPushMatrix()
  }

  def restoreMatrices () {
    glMatrixMode(GL_PROJECTION)
    glPopMatrix()
    glMatrixMode(GL_MODELVIEW)
    glPopMatrix()
  }

  var realWidth : Int = 0
  var realHeight : Int = 0
  
  /**
  * Must be called when the window containing the gl context
  * is resized.
  * @param w the new width of the window
  * @param h the new width of the window
  */
  def resizeWindow (w: Int, h: Int) = {
    if (h==0) throw new Error("resizeWindow with h=0")
    aspectRatio = w.toFloat/h.toFloat

    cameras.foreach(_.aspectRatioChanged(aspectRatio))

    realWidth = w
    realHeight = h
    GL11.glViewport(0, 0, w, h)

    initializeFBO(w,h)
  }

  def registerCamera (c: Camera) = cameras.add(c)
  def unregisterCamera (c: Camera) = cameras.remove(c)

  //check if any OpenGL error occured and print them 
  def checkGLError (diagString: String) {
    val err = GL11.glGetError()
    if (err != 0)
      Console.println("GL Error ("+diagString+") : " + GLU.gluErrorString(err))
  }

  def drawPyramid () {
    GL11.glBegin( GL11.GL_TRIANGLES );             
    GL11.glColor3f(   1.0f,  0.0f,  0.0f ); 
    GL11.glVertex3f(  0.0f,  1.0f,  0.0f ); 
    GL11.glColor3f(   0.0f,  1.0f,  0.0f ); 
    GL11.glVertex3f( -1.0f, -1.0f,  1.0f ); 
    GL11.glColor3f(   0.0f,  0.0f,  1.0f ); 
    GL11.glVertex3f(  1.0f, -1.0f,  1.0f ); 

    GL11.glColor3f(   1.0f,  0.0f,  0.0f ); 
    GL11.glVertex3f(  0.0f,  1.0f,  0.0f ); 
    GL11.glColor3f(   0.0f,  0.0f,  1.0f ); 
    GL11.glVertex3f(  1.0f, -1.0f,  1.0f ); 
    GL11.glColor3f(   0.0f,  1.0f,  0.0f ); 
    GL11.glVertex3f(  1.0f, -1.0f, -1.0f ); 

    GL11.glColor3f(   1.0f,  0.0f,  0.0f ); 
    GL11.glVertex3f(  0.0f,  1.0f,  0.0f ); 
    GL11.glColor3f(   0.0f,  1.0f,  0.0f ); 
    GL11.glVertex3f(  1.0f, -1.0f, -1.0f ); 
    GL11.glColor3f(   0.0f,  0.0f,  1.0f ); 
    GL11.glVertex3f( -1.0f, -1.0f, -1.0f ); 

    GL11.glColor3f(   1.0f,  0.0f,  0.0f ); 
    GL11.glVertex3f(  0.0f,  1.0f,  0.0f ); 
    GL11.glColor3f(   0.0f,  0.0f,  1.0f ); 
    GL11.glVertex3f( -1.0f, -1.0f, -1.0f ); 
    GL11.glColor3f(   0.0f,  1.0f,  0.0f ); 
    GL11.glVertex3f( -1.0f, -1.0f,  1.0f ); 
    GL11.glEnd( );                            
  }

  def setCameraTransform (cam: Camera) {
      updateGLProjFromCamera(cam);
      val matrix = cam.getRotation.getConjugate.getMatrix
      GL11.glMultMatrix(matrix.getFloatBuffer);
      GL11.glTranslatef(-cam.getPosition.x, -cam.getPosition.y, -cam.getPosition.z);
  }
  
  /**
  * Called once before each frame
  */
  def preRender () = {
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT)
    GL11.glLoadIdentity
  }

  private def updateGLProjFromCamera (cam: Camera) : Unit = {
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity

    //Console.println("aspectRatio : " + aspectRatio)
    GLU.gluPerspective(cam.hFov/2.0f, aspectRatio, cam.zNear, cam.zFar)
    
    //FIXME: replace gluPerspective by our own perspective function
/*    val nRect = cam.getNearRect
    checkGLError("updateGLProj:before")   
    Console.println("nRect : " + nRect.xMin + ", "+nRect.xMax+", "+nRect.yMin+","+nRect.yMax+","+cam.zNear+","+cam.zFar)
    GL11.glFrustum(nRect.xMin, nRect.xMax, nRect.yMin, nRect.yMax, cam.zNear, cam.zFar)
    checkGLError("updateGLProj:after")   */
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
  }

  def glColor (color: Color) = GL11.glColor4f(color.r, color.g, color.b, color.a)

  def drawWorldAxis (size: Float) = {
    drawLine(Vector3(0,0,0), Vector3(1,0,0)*size, COL_RED, size)
    drawLine(Vector3(0,0,0), Vector3(0,1,0)*size, COL_GREEN, size)
    drawLine(Vector3(0,0,0), Vector3(0,0,1)*size, COL_BLUE, size)
  }

  //Multiply the current matrix by the given rotation
  def applyRotation (rotation: Quaternion) {
    val matrix = rotation.getMatrix
    GL11.glMultMatrix(matrix.getFloatBuffer)
  }
  
  /**
  * draw coordinates axis, x are red, y are green, z are blue
  */
  def drawAxis (position: Vector3, rotation: Quaternion, size: Float) = {
    GL11.glPushMatrix
    GL11.glLoadIdentity
    val matrix = rotation.getMatrix
    GL11.glMultMatrix(matrix.getFloatBuffer)
    GL11.glTranslatef(position.x, position.y, position.z)

    drawWorldAxis(size)

    GL11.glPopMatrix
  }
  
  def drawLine (from: Vector3, to: Vector3, color: Color, size: Float) = {
    glColor(color)
    GL11.glLineWidth(size)
    GL11.glBegin(GL11.GL_LINES)
      GL11.glVertex3f(from.x, from.y, from.z)
      GL11.glVertex3f(to.x, to.y, to.z)
    GL11.glEnd
    GL11.glLineWidth(1.0f)
    GL11.glColor4f(1.0f,1.0f,1.0f,1.0f)
  }
    
  /**
  * Called once after each frame
  */
  def postRender = {
    GL11.glFlush
  }
}
