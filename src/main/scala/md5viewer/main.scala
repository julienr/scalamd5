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
    if (args.length < 1) {
      Console.println("Usage : <md5mesh directory>")
      return
    }
    Kernel.initialize(args)
    Renderer.registerCamera(camera)
    camera.setPosition(Vector3(-40,106,128))
    camera.setPitch(-0.39f)
    camera.setHeading(0.31f)
    mouseController.registerToEventsManager()
    keyboardController.registerToEventsManager()

    EventsManager.registerKeyPressedCallback(Keyboard.KEY_P, k => {
        //Dump camera informations
        Console.println("camera rotation : " + camera.getRotation.getMatrix)
        Console.printf("camera rotation (pitch=%f, heading=%f, roll=%f)\n", camera.getPitch(), camera.getHeading(), camera.getRoll())
        Console.println("camera position : " + camera.getPosition)
    })

    model = MD5Loader.loadFromDirectory(args(0))

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
