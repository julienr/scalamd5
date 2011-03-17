package md5viewer

import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11._
import utils._
import engine._

object Main extends FrameListener {
  val camera = new Camera();

  val mouseController = new CameraFPSMouseController(camera, 0.001f)
  var keyboardController = new CameraFPSKeyboardController(camera)

  var model : MD5Model = null
  var anim : MD5Anim = null

  var glProgram : GLSLProgram = null
  
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

    //Load shaders
    Renderer.checkGLError("Before shaders")
    val vs = new VertexShader(io.Source.fromFile("data/shaders/vertex.glsl").mkString)
    val fs = new FragmentShader(io.Source.fromFile("data/shaders/fragment.glsl").mkString)
    glProgram = new GLSLProgram(vs, fs)

    model = MD5Loader.loadFromDirectory(args(0))
    if (args.length > 1) {
      anim = MD5Loader.loadAnim(args(1))
      Console.println("Anim loaded")
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

    glProgram.bind()
    model.draw(glProgram)
    glProgram.unbind()
  }

  @Override
  def move (elapsedTime: Float) {
    keyboardController.control(elapsedTime, 20.0f)
    if (anim != null) {
      anim.animate(model, elapsedTime)
    }
    FPSCounter.countFrame(elapsedTime)
    //System.out.println("Move : " + elapsedTime);
  }

  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
  }

}
