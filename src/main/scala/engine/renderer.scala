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

  def initialize (w: Int, h: Int) {
    //initialization code
    GL11.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
    GL11.glEnable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glCullFace(GL11.GL_BACK)
    GL11.glFrontFace(GL11.GL_CW)
    resizeWindow(w,h)
  }

  def saveViewport () {
    glPushAttrib(GL_VIEWPORT_BIT)
  }

  def restoreViewport () {
    glPopAttrib()
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
  }

  def registerCamera (c: Camera) = cameras.add(c)
  def unregisterCamera (c: Camera) = cameras.remove(c)

  //check if any OpenGL error occured and print them 
  def checkGLError (diagString: String) {
    val err = GL11.glGetError()
    if (err != 0)
      Console.println("GL Error ("+diagString+") : " + GLU.gluErrorString(err))
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
    
    //TODO: replace gluPerspective by our own perspective function
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
