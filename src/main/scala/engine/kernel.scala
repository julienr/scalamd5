package engine

import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode

import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.util.Timer

import scala.collection.mutable.HashMap

import utils._

trait FrameListener {
  //Returns the camera that should be used for rendering
  def currentCamera () : Camera
  def render ()
  def move (elapsedTime: Float)
}

object Kernel {
  var loop = true

  val width = 800 
  val height = 600

  val timer = new Timer

  def initialize (args: Array[String]) {
    val caption = "Scalissor !";
    try {
      Display.setDisplayMode(new DisplayMode(width, height))
      Display.setTitle(caption)
      Display.create
      Keyboard.create
      Mouse.create

      Mouse.setGrabbed(true)

      Keyboard.enableRepeatEvents(false)

      Renderer.initialize(width, height)
    } catch {
      case e:LWJGLException => 	System.out.println("LWJGL Initialization error : ")
        e.printStackTrace
    }
  }

  //main loop. Use createFrameListenet to create the frameListener as soon as
  //initialization is complete
  def mainLoop(createFrameListener: Unit => FrameListener) = {
    val frameListener = createFrameListener()

    timer.reset
    while(loop) {
      Timer.tick
      EventsManager.handleEvents
      Renderer.preRender(frameListener.currentCamera)

      frameListener.move(timer.getTime)
      //FIXME: should we let the timer run instead ?
      timer.reset
      frameListener.render

      Renderer.postRender
      Display.update
    }

    Keyboard.destroy
    Display.destroy
  }
}
