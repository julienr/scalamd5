package engine

import org.lwjgl.LWJGLException
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.DisplayMode
import org.lwjgl.opengl.GLContext
import org.lwjgl.opengl.ContextCapabilities
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

      checkCapabilities(GLContext.getCapabilities())

      Renderer.initialize(width, height)
    } catch {
      case e:LWJGLException => 	System.out.println("LWJGL Initialization error : ")
        e.printStackTrace
    }
  }

  def checkCapabilities (cap : ContextCapabilities) {
    Console.println("Context supported OpenGL versions")
    Console.println("GL 1.1 : " + cap.OpenGL11)
    Console.println("GL 1.2 : " + cap.OpenGL12)
    Console.println("GL 1.3 : " + cap.OpenGL13)
    Console.println("GL 1.4 : " + cap.OpenGL14)
    Console.println("GL 1.5 : " + cap.OpenGL15)
    Console.println("GL 2.0 : " + cap.OpenGL20)
    Console.println("GL 2.1 : " + cap.OpenGL21)
    Console.println("GL 3.0 : " + cap.OpenGL30)
    Console.println("GL 3.1 : " + cap.OpenGL31)
    Console.println("GL 3.2 : " + cap.OpenGL32)
    Console.println("GL 3.3 : " + cap.OpenGL33)
    Console.println("GL 4.0 : " + cap.OpenGL40)
    Console.println("GL 4.1 : " + cap.OpenGL41)
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
