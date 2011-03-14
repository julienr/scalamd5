package engine
import utils._
import scala.collection.mutable.HashMap
import org.lwjgl.input.Keyboard

//A FPS-like camera controller
//bind onMouseMovent as EventsManager's mouseMove callback
class CameraFPSMouseController (cam: Camera, sensi: Float) {
  var sensitivity = sensi
  
  //holds mouse rotation
  /*var totX : Float = 0.0f
  var totY : Float = 0.0f*/

  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
    //Console.println("Mouse move from " + oldPos + " to " + newPos)
    val d = newPos-oldPos
    
    val rX = d.y*sensitivity
    val rY = d.x*sensitivity
    cam.changePitch(rX)
    cam.changeHeading(rY)
    /*val qX = Quaternion(rX, Vector3(1,0,0))
    val qY = Quaternion(-rY, Vector3(0,1,0))
    cam.setRotation((qX*qY*cam.getRotation).getNormalized)*/
    
/*    totX -= d.y*sensitivity
    totY -= d.x*sensitivity
    //Console.println("totX="+totX+", totY="+totY)
    totX = MathUtils.clamp(totX, -MathUtils.PI_2, MathUtils.PI_2).toFloat
    val qX = new Quaternion(-totX.toFloat, Vector3(1,0,0))
    val qY = new Quaternion(totY.toFloat, Vector3(0,1,0))
    cam.setRotation((qY*qX).getNormalized)*/
  }

  def registerToEventsManager () {
    EventsManager.registerMouseMoveCallback(onMouseMove)
  }

}

class CameraFPSKeyboardController (cam: Camera) {
  object Action extends Enumeration {
    type Action = Value
    val FORWARD = Value(Keyboard.KEY_W)
    val BACKWARD = Value(Keyboard.KEY_S)
    val STRAFE_LEFT = Value(Keyboard.KEY_A)
    val STRAFE_RIGHT = Value(Keyboard.KEY_D)

  }
  import Action._

  val state = new HashMap[Int, Boolean]
  Action.values.foreach((v: Value) => state(v.id) = false)

  def registerToEventsManager () {
    EventsManager.registerDefaultKeyPressedCallback(onKeyPressed)
    EventsManager.registerDefaultKeyReleasedCallback(onKeyReleased)
  }

  def unregisterFromEventsManager () {
    EventsManager.registerDefaultKeyPressedCallback(null)
    EventsManager.registerDefaultKeyReleasedCallback(null)
  }

  def onKeyPressed (keyCode: Int) {
    if (state.contains(keyCode))
      state(keyCode) = true
  }

  def onKeyReleased (keyCode: Int) {
    if (state.contains(keyCode))
      state(keyCode) = false
  }

  def control (elapsedS: Float, moveSpeed: Float) {
    var xmove = 0
    var zmove = 0
    if (state(FORWARD.id)) zmove -= 1
    if (state(BACKWARD.id)) zmove += 1
    if (state(STRAFE_LEFT.id)) xmove -= 1
    if (state(STRAFE_RIGHT.id)) xmove +=1

    cam.moveRelative(Vector3(xmove, 0, zmove)*elapsedS*moveSpeed)
  }
}

