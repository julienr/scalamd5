package engine
import org.lwjgl.opengl.GL11._
import org.newdawn.slick.opengl._
import org.newdawn.slick._
import java.io.IOException
import java.io.FileInputStream
import java.nio.ByteBuffer
import org.lwjgl.BufferUtils


object TexUtils {
  //load given texture from file
  def loadTex (file: String, filter: Int) : Texture = {
    try {
      Console.println("loading : " + file)
      TextureLoader.getTexture("TGA", new FileInputStream(file), filter)
    } catch {
      //FIXME: Should return a white texture
      case ioe: IOException => Console.println(ioe); null
    }
  }

  //create a new white texture
  def loadWhiteTex () : Texture = {
    /*val img = new Image(new EmptyImageData(4,4))
    img.getTexture()*/
    return createMonoTexture(255.toByte,255.toByte,255.toByte)
  }

  //create a new white texture
  def loadBlackTex () : Texture = {
    return createMonoTexture(0,0,0)
  }

  //create a texture with a single color
  def createMonoTexture (r: Int, g: Int, b: Int) : Texture = {
    val id = glGenTextures()
    glBindTexture(GL_TEXTURE_2D, id)
    val buff = BufferUtils.createByteBuffer(3) 
    buff.put(r.toByte)
    buff.put(g.toByte)
    buff.put(b.toByte)
    buff.rewind()
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGB, GL_UNSIGNED_BYTE, buff)

    Renderer.checkGLError("mono texture")

    return new TextureImpl("mono", GL_TEXTURE_2D, id)
  }

  //An image filled with a single color
  /*class MonoImage (r: Byte, g: Byte, b: Byte) extends ImageData {

    override def getDepth(): Int = 24
    override def getHeight(): Int = 1
    override def getWidth(): Int = 1
    override def getTexHeight(): Int = 1
    override def getTexWidth(): Int = 1
    override def getImageBufferData(): ByteBuffer = buff
  }*/
}

// vim: set ts=2 sw=2 et:
