package md5viewer

import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL13._
import org.lwjgl._
import org.lwjgl.util.glu.GLU._


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
    var rotSpeed = 0.3
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
      glColor4f(1,0,0,1)
      glBegin(GL_POINTS)
      glVertex3f(position.x, position.y, position.z)
      glEnd()
    }
  }

  class SpotLight {
    val rotCenter = Vector3(0,110,0)
    var rotAngle = 0.0
    var rotSpeed = 0.3
    val rotRadius = 50

    var position = rotCenter
    var lookAt = Vector3(0,0,0)

    def updatePos (elapsedS: Float) {
      position = rotCenter+Vector3(math.cos(rotAngle).toFloat, 0, math.sin(rotAngle).toFloat)*rotRadius
      rotAngle += rotSpeed*elapsedS
/*      if (rotAngle > 2*MathUtils.PI)
        rotAngle -= 2*MathUtils.PI*/

      if (rotAngle > MathUtils.PI)
        rotSpeed *= -1
      if (rotAngle < 0)
        rotSpeed *= -1
    }

    def draw () {
      glActiveTexture(GL_TEXTURE0)
      glDisable(GL_TEXTURE_2D)
      glActiveTexture(GL_TEXTURE1)
      glDisable(GL_TEXTURE_2D)

      //light
      glPointSize(5.0f)
      glColor4f(1,0,0,1)
      glBegin(GL_POINTS)
      glVertex3f(position.x, position.y, position.z)
      glEnd()
    }
  }

  val light = new SpotLight()
  
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
    val vs = new VertexShader(io.Source.fromFile("data/shaders/spot_vertex.glsl").mkString)
    val fs = new FragmentShader(io.Source.fromFile("data/shaders/spot_fragment.glsl").mkString)
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
    Renderer.saveMatrices()
    Renderer.bindFBO() 
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    gluPerspective(45.0f, 1.0f, 1.0f, 1000.0f)
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    gluLookAt(light.position.x, light.position.y, light.position.z,
              light.lookAt.z, light.lookAt.y, light.lookAt.z,
              0,1,0)
    model.glentity.draw(null)
    Renderer.unbindFBO()
    Renderer.restoreMatrices()


    //Normal rendering
    Renderer.setCameraTransform(camera)
    Renderer.drawWorldAxis(1);

    glProgram.bind()
    glProgram.setUniform("lightPos", camera.getRotation.getConjugate.rotate(light.position-camera.getPosition))
    glProgram.setUniform("eyeSpotDir", camera.getRotation.getConjugate.rotate(light.lookAt-light.position-camera.getPosition))
    glProgram.setUniform("attVector", Vector3(1.0f, 0, 0));
    glProgram.setUniform("spotCosCutoff", math.cos(0.3).toFloat)
    glProgram.setUniform("spotExp", 50)

    model.glentity.draw(glProgram)
    glProgram.unbind()

    //model.drawNormals()
    light.draw()

    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    glOrtho(0, 640, 480, 0, 0, 1)
    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()

    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_TEXTURE_COORD_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glActiveTexture(GL_TEXTURE0)
    glEnable(GL_TEXTURE_2D)
    //model.meshes(0).colorTex.bind()
    //TODO: should probably write a shader to draw depth texture in a meaningfull way
    glBindTexture(GL_TEXTURE_2D, Renderer.depthTextureId)

    glDisable(GL_CULL_FACE)

    glColor4f(1,1,1,1)
    glBegin(GL_QUADS)
      glVertex2i(540,380)
      glTexCoord2f(0,0)

      glVertex2i(640,380)
      glTexCoord2f(1,0)

      glVertex2i(640,480)
      glTexCoord2f(1,1)

      glVertex2i(540,480)
      glTexCoord2f(0,1)
    glEnd()

    glDisable(GL_TEXTURE_2D)
    glEnable(GL_CULL_FACE)
  }

  @Override
  def move (elapsedTime: Float) {
    keyboardController.control(elapsedTime, 40.0f)
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
