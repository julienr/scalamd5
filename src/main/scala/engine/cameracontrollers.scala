package engine
import utils._
import scala.collection.mutable.HashMap
import org.lwjgl.input.Keyboard

//A FPS-like camera controller
//bind onMouseMovent as EventsManager's mouseMove callback
class CameraFPSMouseController (cam: Camera) {
  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
    Console.println("Mouse move from " + oldPos + " to " + newPos)
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

  val state = new HashMap[Action, Boolean]
  Action.values.foreach(state(_) = false)

  def registerToEventsManager () {
    EventsManager.registerDefaultKeyPressedCallback(onKeyPressed)
    EventsManager.registerDefaultKeyReleasedCallback(onKeyReleased)
  }

  def unregisterFromEventsManager () {
    EventsManager.registerDefaultKeyPressedCallback(null)
    EventsManager.registerDefaultKeyReleasedCallback(null)
  }

  def onKeyPressed (keyCode: Int) {
    if (Action.values.contains(Action(keyCode)))
      state(Action(keyCode)) = true
  }

  def onKeyReleased (keyCode: Int) {
    if (Action.values.contains(Action(keyCode)))
      state(Action(keyCode)) = false
  }

  def control (elapsedS: Float, moveSpeed: Float) {
    var xmove = 0
    var zmove = 0
    if (state(FORWARD)) zmove -= 1
    if (state(BACKWARD)) zmove += 1
    if (state(STRAFE_LEFT)) xmove -= 1
    if (state(STRAFE_RIGHT)) xmove +=1

    cam.moveRelative(Vector3(xmove, 0, zmove)*elapsedS*moveSpeed)
  }
}

