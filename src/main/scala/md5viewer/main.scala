package md5viewer

import org.lwjgl.input.Keyboard
import utils._
import engine._

object Main extends FrameListener {
  val camera = new Camera();

  val mouseController = new CameraFPSMouseController(camera, 0.001f)
  var keyboardController = new CameraFPSKeyboardController(camera)

  var model : MD5Model = null
  
  def main(args: Array[String]) {
    Kernel.initialize(args)
    Renderer.registerCamera(camera)
    camera.setPosition(Vector3(-40,106,128))
    camera.setRotation(Quaternion(0.51f, Vector3(-0.80f, -0.58f, -0.12f)))
    mouseController.registerToEventsManager()
    keyboardController.registerToEventsManager()

    EventsManager.registerKeyPressedCallback(Keyboard.KEY_P, k => {
        //Dump camera informations
        Console.println("camera rotation : " + camera.getRotation.getMatrix)
        Console.println("camera rotation (quat) : angle="+camera.getRotation.getAngle()+", axis="+camera.getRotation.getAxis())
        Console.println("camera position : " + camera.getPosition)
    })

    val p = new MD5Loader.MD5ParserCombinators
    val lines = io.Source.fromFile("data/imp.md5mesh").mkString
    p.parseAll(p.model, MD5Loader.removeComments(lines)) match {
      case p.Success(m,_) =>  model = m
      case x => throw new Exception("Error loading : " + x)
    }

    Kernel.mainLoop(Unit => this )
  }


  @Override
  def currentCamera () : Camera = camera

  @Override
  def render () {
    //System.out.println("Render");
    Renderer.drawWorldAxis(1);
    //Renderer.drawPyramid()

    model.draw()
  }

  @Override
  def move (elapsedTime: Float) {
    keyboardController.control(elapsedTime, 20.0f)
    //System.out.println("Move : " + elapsedTime);
  }

  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
  }

}
