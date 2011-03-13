package engine
import utils._
import org.lwjgl.opengl._
import org.lwjgl.util.glu._
import scala.collection.mutable.HashSet

object Renderer {
  var aspectRatio = 0.0f 

  val cameras = new HashSet[Camera]()

  //always store the last camera used for rendering
  private var lastRenderedCamera : Camera = null

  /**
   * We work with virtual screen coordinates to get resolution independence.
   * Whatever the user resolution is, the usable area to place our object is (screenWidth, screenHeight)
   * FIXME: this doesn't work with 16/9 ratio or anything else (well it will work, but distorted)
   */
  val screenWidth = 800
  val screenHeight = 600

  def initialize (w: Int, h: Int) {
    //initialization code
    GL11.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)
    GL11.glDisable(GL11.GL_DEPTH_TEST)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_DITHER)
/*    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)*/
    resizeWindow(w,h)
  }

  var realWidth = 0
  var realHeight = 0
  
  /**
  * Must be called when the window containing the gl context
  * is resized.
  * @param w the new width of the window
  * @param h the new width of the window
  */
  def resizeWindow (w: Int, h: Int) = {
    if (h==0) throw new Error("resizeWindow with h=0")
    aspectRatio = h.toFloat/w.toFloat

    cameras.foreach(_.aspectRatioChanged(aspectRatio))

    realWidth = w
    realHeight = h
    GL11.glViewport(0, 0, w.toInt, h.toInt)
    lastRenderedCamera = null //will trigger a perspective recalculation
  }

  def registerCamera (c: Camera) = cameras.add(c)
  def unregisterCamera (c: Camera) = cameras.remove(c)

  //check if any OpenGL error occured and print them 
  def checkGLError (diagString: String) {
    val err = GL11.glGetError()
    if (err != 0)
      Console.println("GL Error ("+diagString+") : " + GLU.gluErrorString(err))
  }

  def printMatrix (matrix: java.nio.FloatBuffer) {
    for (i <- Range(0,4)) {
      Console.printf("[")
      for (j <- Range(0,4)) {
        Console.printf("%f ", matrix.get(i*4+j))
      }
      Console.printf("]\n")
    }
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
  
  /**
  * Called once before each frame
  */
  def preRender (cam: Camera) = {
    //If camera has changed, we need to recalculate projection
    if (cam != lastRenderedCamera) {
      updateGLProjFromCamera(cam);
      lastRenderedCamera = cam
    }
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT)
    GL11.glLoadIdentity

    val matrix = cam.rotation.getConjugate.getMatrix
    /*Console.println("camera rotation : ")
    printMatrix(matrix)

    Console.println("current modelview : ")
    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix)
    printMatrix(matrix)

    
    checkGLError("preRender:beforeMult")*/
    GL11.glMultMatrix(matrix.getFloatBuffer);
/*    Console.println(cam.position)

    val modelview = java.nio.FloatBuffer.allocate(16)
    Console.println("final modelview : ")
    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview)
    printMatrix(modelview)*/

    GL11.glTranslatef(-cam.position.x, -cam.position.y, -cam.position.z);
  }

  private def updateGLProjFromCamera (cam: Camera) : Unit = {
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity

    GLU.gluPerspective(cam.hFov/2, 3/4.0f, cam.zNear, cam.zFar)
    
    //FIXME: replace gluPerspective by our own perspective function
/*    val nRect = cam.getNearRect
    checkGLError("updateGLProj:before")   
    Console.println("nRect : " + nRect.xMin + ", "+nRect.xMax+", "+nRect.yMin+","+nRect.yMax+","+cam.zNear+","+cam.zFar)
    GL11.glFrustum(nRect.xMin, nRect.xMax, nRect.yMin, nRect.yMax, cam.zNear, cam.zFar)
    checkGLError("updateGLProj:after")   */
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
  }

  def glColor (color: Color) = GL11.glColor4f(color.r, color.g, color.b, color.a)

/*  def drawLines (points: List[Vector2], color: Color) = {
    GL11.glPushAttrib(GL11.GL_CURRENT_BIT)
    glColor(color)
    GL11.glBegin(GL11.GL_LINE_LOOP)
    points.foreach(grVertex2f)
    GL11.glEnd
    GL11.glPopAttrib
  }*/
  
  /**
  * draw coordinates axis, x are red, y are green, z are blue
  */
  def drawAxis (position: Vector3, rotation: Quaternion, size: Float) = {
    GL11.glPushMatrix
    GL11.glLoadIdentity
    val matrix = rotation.getMatrix
    GL11.glMultMatrix(matrix.getFloatBuffer)
    GL11.glTranslatef(position.x, position.y, position.z)

    drawLine(Vector3(0,0,0), Vector3(1,0,0)*size, COL_RED, size)
    drawLine(Vector3(0,0,0), Vector3(0,1,0)*size, COL_GREEN, size)
    drawLine(Vector3(0,0,0), Vector3(0,0,1)*size, COL_BLUE, size)

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
