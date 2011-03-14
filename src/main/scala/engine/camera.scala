package engine;
import org.lwjgl.opengl._
import utils._

/**
* Each camera is identified by a unique-ID, this object is
* used to create them
*/
private object CamID {
  private var currentId : Int = 0
  def getNewID : Int = {
    currentId += 1
    return currentId
  }
}

class Camera () {
  private var rotation = new Quaternion(0.0f, Vector3(0,1,0))
  private var position = new Vector3
  private var frustum : Frustum = null
  
  val hFov : Float = 90.0f
  //var vFov : Float = 0.0f
  var aspectRatio : Float = 4.0f/3.0f
  var zNear : Float = 0.1f //zNear MUST be positive
  var zFar : Float = 1000.0f //zFar MUST be positive
  var e : Float = 0.0f //focal length
  
  var id : Int = CamID.getNewID
  
  /**
  * Rotate the camera from mouse displacement
  * @param rx relative horizontal movement of mouse
  * @param ry relative vertical movement of mouse
  */
  /*def rotateFromMouse (rx: Int, ry: Int) = {
    totX -= rx.toDouble*sensitivity
    totY -= ry.toDouble*sensitivity
    //make sure the player can't do 2pi rotations 
    //FIXME: shouldn't it be a parameter of the camera ?
    totX = MathUtils.clamp(totX.toFloat, -TG_PI_2, TG_PI_2).toFloat
    val qY = new Quaternion(totY.toFloat, Vector3(0,1,0))
    val qX = new Quaternion(totX.toFloat, Vector3(1,0,0))
    rotation = (qY*qX).getNormalized
    
    invalidateView
  }*/

  //Once this Camera is registered to the Renderer, this will get called when
  //aspect ratio changes
  def aspectRatioChanged (newAR: Float) {
    aspectRatio = newAR
    invalidateView
  }

  def setPosition (v: Vector3) = {
    position.load(v)
    invalidateView
  }

  def setRotation (q: Quaternion) {
    rotation.load(q)
    invalidateView
  }

  def getRotation = rotation
  def getPosition = position

  /**
  * Move the camera with a movement expressed in world coordinates
  * @param v the movement relative to the world coordinate system
  */
  def move (v: Vector3) = {
    position += v
    invalidateView
  }
  
  /**
  * Move the camera with a movement expressed on its local axis
  * @param v the movement relative to the camera's local axis
  */
  def moveRelative (v: Vector3) = {
    position += rotation.rotate(v)
    invalidateView
  }
  
  /** 
  * Recreate the frustum plane after a movement/rotation of the camera
  */
  private def invalidateView = {
    // calculate new frustum planes, standard OpenGL is assumed, that is :
    // - x poInts right
    // - y poInts upward
    // - z poInts in the opposite direction that in which the camera poInts
    // => right-handed
    e = 1.0f/scala.math.tan((hFov*MathUtils.DEG_TO_RAD)/2.0f).toFloat
    //vFov = 2.0f*scala.math.atan(aspectRatio/e).toFloat
    
    //Frustum planes are in world coordinates
    val localZ = rotation.zAxis

    frustum = new Frustum(position, 
          new Plane(position-(localZ*zNear), rotation.rotate(new Vector3(0,0,-1))), //near
          new Plane(position-(localZ*zFar), rotation.rotate(new Vector3(0,0,1))), //far
          new Plane(position, rotation.rotate(new Vector3(e,0,-1))):: //left
          new Plane(position, rotation.rotate(new Vector3(-e, 0, -1))):: //right
          new Plane(position, rotation.rotate(new Vector3(0, -e, -aspectRatio))):: //top
          new Plane(position, rotation.rotate(new Vector3(0, e, -aspectRatio)))::Nil) //bottom
    
  }
  
  /**
  * Get the coordinates of the four edges of an axis-aligned rectangle that
  * represents the near plane. This can be used to get the four parameters to
  * pass to glFrustum
  * @return a Rectangle with the following coordinates :
  *	    yMax
  *     |---------|
  *     |         |
  * xMin|         |xMax
  *     |---------|
  * 	    yMin
  */
  def getNearRect : Rectangle = {
    val rightX = zNear/e
    val topY = (zNear*aspectRatio)/e
    return new Rectangle(rightX, -rightX, topY, -topY)
  }
  
  /**
  * Very similar to getNearRect but for the far rect
  */
  def getFarRect : Rectangle = getFarRect(zFar)
  // scale will be used instead of zFar for the far plane distance (useful for drawing)
  def getFarRect (far: Float) : Rectangle = {
    val rightX = far/e
    val topY = far*aspectRatio/e
    return new Rectangle(rightX, -rightX, topY, -topY)
  }
  
  /**
  * Draw a debug frustum
  * @param gl the current GL context
  */
  def drawFrustum (scale: Float)= {
    GL11.glPushMatrix
    
    //transform to go from cam space to model space
    GL11.glTranslatef(position.x, position.y, position.z)
    val matrix = rotation.getMatrix
    GL11.glMultMatrix(matrix.getFloatBuffer)
    
    //draw cam space axis
    Renderer.drawAxis(position, rotation, 5.0f)
    
    GL11.glColor3f(0.0f,0.0f,1.0f)
    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
    GL11.glDisable(GL11.GL_CULL_FACE)
    
    //draw near plane
    var nRect = getNearRect
    GL11.glBegin(GL11.GL_QUADS)
      GL11.glVertex3f(nRect.xMin, nRect.yMin, -zNear)
      GL11.glVertex3f(nRect.xMin, nRect.yMax, -zNear)
      GL11.glVertex3f(nRect.xMax, nRect.yMax, -zNear)
      GL11.glVertex3f(nRect.xMax, nRect.yMin, -zNear)
    GL11.glEnd()
    
    //draw far plane, we don't really draw it at zFar for obvious reason
    var fRect = getFarRect(scale)
    GL11.glBegin(GL11.GL_QUADS)
      GL11.glVertex3f(fRect.xMin, fRect.yMin, -scale)
      GL11.glVertex3f(fRect.xMin, fRect.yMax, -scale)
      GL11.glVertex3f(fRect.xMax, fRect.yMax, -scale)
      GL11.glVertex3f(fRect.xMax, fRect.yMin, -scale)
    GL11.glEnd()
    
    //draw side planes
    GL11.glBegin(GL11.GL_QUADS)
      //right
      GL11.glVertex3f(nRect.xMin, nRect.yMin, -zNear)
      GL11.glVertex3f(nRect.xMin, nRect.yMax, -zNear)
      GL11.glVertex3f(fRect.xMin, fRect.yMax, -scale)
      GL11.glVertex3f(fRect.xMin, fRect.yMin, -scale)
      
      //left
      GL11.glVertex3f(nRect.xMin, nRect.yMin, -zNear)
      GL11.glVertex3f(nRect.xMin, nRect.yMax, -zNear)
      GL11.glVertex3f(fRect.xMin, fRect.yMax, -scale)
      GL11.glVertex3f(fRect.xMin, fRect.yMin, -scale)
      
      //top
      GL11.glVertex3f(nRect.xMin, nRect.yMax, -zNear)
      GL11.glVertex3f(fRect.xMin, fRect.yMax, -scale)
      GL11.glVertex3f(fRect.xMax, fRect.yMax, -scale)
      GL11.glVertex3f(nRect.xMax, nRect.yMax, -zNear)
      
      //bottom
      GL11.glVertex3f(nRect.xMin, nRect.yMin, -zNear)
      GL11.glVertex3f(fRect.xMin, nRect.yMin, -scale)
      GL11.glVertex3f(fRect.xMax, nRect.yMin, -scale)
      GL11.glVertex3f(nRect.xMin, nRect.yMin, -zNear)
    GL11.glEnd()
    
    /* draw normals
    * middle is the average of the sides extremities
    * planes in the frustum struct are given in world coords, so inverse the rotation to get
    * them back in local coords
    */
    def _drawNormal0(middle: Vector3, norm: Vector3) : Unit = Renderer.drawLine(middle, middle + rotation.getConjugate.rotate(norm*5), COL_BLACK, 1.0f)
    def _drawNormal(middle: Vector3, planeNum: Int) : Unit = _drawNormal0(middle, frustum.sidePlanes(planeNum).normal)

    //left 
    _drawNormal(new Vector3((nRect.xMin+fRect.xMin)/2, (nRect.yMin+nRect.yMax+fRect.yMax+fRect.yMin)/4, -(zNear+scale)/2), 0)
    //right
    _drawNormal(new Vector3((nRect.xMax+fRect.xMax)/2, (nRect.yMin+nRect.yMax+fRect.yMax+fRect.yMin)/4, -(zNear+scale)/2), 1)
    //top
    _drawNormal(new Vector3((nRect.xMin+fRect.xMin+fRect.xMax+nRect.xMax)/4, (nRect.yMax+fRect.yMax)/2, -(zNear+scale)/2), 2)
    //bottom
    _drawNormal(new Vector3((nRect.xMin+fRect.xMin+fRect.xMax+nRect.xMax)/4, (nRect.yMin+fRect.yMin)/2, -(zNear+scale)/2), 3)
    //near
    _drawNormal0(new Vector3((nRect.xMin+nRect.xMax)/2, (nRect.yMin+nRect.yMax)/2, -zNear), frustum.nearPlane.normal)
    //far
    _drawNormal0(new Vector3((fRect.xMin+fRect.xMax)/2, (fRect.yMin+fRect.yMax)/2, -scale), frustum.farPlane.normal)
    
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)
    GL11.glColor3f(1.0f,1.0f,1.0f)
    
    GL11.glPopMatrix
  }

}
