package md5viewer

import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl._


import utils._
import engine._

object Main extends FrameListener {
  val camera = new Camera();

  val mouseController = new CameraFPSMouseController(camera, 0.001f)
  var keyboardController = new CameraFPSKeyboardController(camera)

  var model : MD5Model = null
  var anim : MD5Anim = null

  var glProgram : GLSLProgram = null

  //point light position
  class PointLight {
    val rotCenter = Vector3(0,90,0)
    var rotAngle = 0.0
    val rotSpeed = 0.3
    val rotRadius = 50

    var position = rotCenter

    def updatePos (elapsedS: Float) {
      position = rotCenter+Vector3(math.cos(rotAngle).toFloat, 0, math.sin(rotAngle).toFloat)*rotRadius
      rotAngle += rotSpeed*elapsedS
      if (rotAngle > 2*MathUtils.PI)
        rotAngle -= 2*MathUtils.PI
    }

    def draw () {
      glActiveTexture(GL_TEXTURE0)
      glDisable(GL_TEXTURE_2D)
      glActiveTexture(GL_TEXTURE1)
      glDisable(GL_TEXTURE_2D)

      //light
      glPointSize(5.0f)
      glColor4f(1,0,0,0)
      glBegin(GL_POINTS)
      glVertex3f(position.x, position.y, position.z)
      glEnd()
    }
  }

  val light = new PointLight()
  
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

    model = MD5Loader.loadModel(args(0))
    if (args.length > 1) {
      anim = MD5Loader.loadAnim(args(1))
      Console.println("Anim loaded")
    }

    Kernel.mainLoop(Unit => this )
  }


  @Override
  def render () {
    Renderer.setCameraTransform(camera)
    //System.out.println("Render");
    Renderer.drawWorldAxis(1);
    //Renderer.drawPyramid()

    glProgram.bind()
    //Calculate view-space light pos
    glProgram.setUniform("lightPos", camera.getRotation.getConjugate.rotate(light.position-camera.getPosition))
    model.glentity.draw(glProgram)
    glProgram.unbind()

    //model.drawNormals()

    light.draw()
  }

  @Override
  def move (elapsedTime: Float) {
    keyboardController.control(elapsedTime, 20.0f)
    if (anim != null) {
      anim.animate(model, elapsedTime)
    }
    FPSCounter.countFrame(elapsedTime)
    //System.out.println("Move : " + elapsedTime);

    light.updatePos(elapsedTime)

  }

  def onMouseMove (oldPos: Vector2, newPos: Vector2) {
  }

}
