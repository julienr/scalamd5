package md5viewer

import utils._
import engine._

object Main extends FrameListener {
  val camera = new Camera();

  val mouseController = new CameraFPSMouseController(camera, 0.001f)
  var keyboardController = new CameraFPSKeyboardController(camera)
  
  def main(args: Array[String]) {
    Kernel.initialize(args)
    Renderer.registerCamera(camera)
    camera.setPosition(Vector3(1.5f,0,8))
    mouseController.registerToEventsManager()
    keyboardController.registerToEventsManager()
    Kernel.mainLoop(Unit => this )
  }

  @Override
  def currentCamera () : Camera = camera

  @Override
  def render () {
    //System.out.println("Render");
    Renderer.drawAxis(Vector3(0,0,0), new Quaternion(), 5);
    Renderer.drawPyramid()
  }

  @Override
  def move (elapsedTime: Float) {
    keyboardController.control(elapsedTime, 5.0f)
    //System.out.println("Move : " + elapsedTime);
  }

  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
  }

}
