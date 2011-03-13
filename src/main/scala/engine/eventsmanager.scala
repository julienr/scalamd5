package engine
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

import utils._

import scala.collection.mutable.HashMap

object EventsManager {
  var mousePrevPos = new Vector2(0,0)

  type KeyCallback = Int => Unit
  type MouseClickCallback = () => Unit
  //First argument is previous position, second is new
  type MouseMoveCallback = (Vector2, Vector2) => Unit

  private val keyPressedCallbacks = new HashMap[Int, KeyCallback]
  private val keyReleasedCallbacks = new HashMap[Int, KeyCallback]
  private val mouseClickCallbacks = new HashMap[Int, MouseClickCallback]
  private var mouseMoveCallback: MouseMoveCallback = null

  /**
   * Assign the given callback to the given key in the given map
   * Raise an exception if there already is a callback for the key
   */
  private def _assignCallback[T] (reg: HashMap[Int, T], k: Int, callback: T) = {
    if (reg.contains(k))
      throw new Exception ("Trying to override callback for k="+k+" in reg :"+reg)
    reg(k) = callback
  }

  def registerKeyPressedCallback (key: Int, callback: KeyCallback) = _assignCallback(keyPressedCallbacks, key, callback)
  def registerKeyReleasedCallback (key: Int, callback: KeyCallback) = _assignCallback(keyReleasedCallbacks, key, callback)

  //Register multiple callbacks at once
  def registerKeyPressedCallbacks (callbacks: List[(Int, KeyCallback)]) = 
    callbacks.map (p => registerKeyPressedCallback(p._1, p._2))
  def registerKeyReleasedCallbacks (callbacks: List[(Int, KeyCallback)]) =
    callbacks.map (p => registerKeyReleasedCallback(p._1, p._2))

  def registerMouseClickCallback (button: Int, callback: MouseClickCallback) = _assignCallback(mouseClickCallbacks, button, callback)
  def registerMouseMoveCallback (callback: MouseMoveCallback) = {
    if (mouseMoveCallback != null)
      throw new Exception ("Trying to override mouse move callback")
    mouseMoveCallback = callback
  }

  def handleEvents = {
    // handle a single keyboard event
    def handleKeyEvent (state:Boolean, event: Int): Unit = {
      if (state) { //key down
        event match {
          case Keyboard.KEY_ESCAPE => Kernel.loop = false
          case _ => if (keyPressedCallbacks.contains(event)) keyPressedCallbacks(event)(event)
        }
      } else {
        event match {
          case Keyboard.KEY_ESCAPE => 
          case _ => if (keyReleasedCallbacks.contains(event)) keyReleasedCallbacks(event)(event)
        }
      }
    }

    //Keyboard events
    var hasEvent = Keyboard.next
    while (hasEvent) {
      handleKeyEvent(Keyboard.getEventKeyState, Keyboard.getEventKey)
      hasEvent = Keyboard.next
    }

    //Mouse events
    hasEvent = Mouse.next
    while (hasEvent) {
      val button = Mouse.getEventButton
      val newPos = new Vector2(Mouse.getX, Mouse.getY)

      //There has been a click
      if (button != -1 && mouseClickCallbacks.contains(button))
        mouseClickCallbacks(button)()

      //There has been a movement
      if (!((newPos-mousePrevPos) ~= (0.0f, 0.0f))) {
          if (mouseMoveCallback != null)
            mouseMoveCallback(mousePrevPos, newPos)
          mousePrevPos = newPos
      }
      hasEvent = Mouse.next
    }
  }
}
