package engine
import utils._
import org.lwjgl.opengl.GL20._
import java.nio.FloatBuffer
import collection.mutable.HashMap

abstract class Shader (val source: String, glType: Int) {
  val id = glCreateShader(glType)
  glShaderSource(id, source)
  glCompileShader(id)
  Renderer.checkGLError("Shader compilation")
  val err = glGetShaderInfoLog(id, 1024).trim()
  if (!err.equals(""))
    Console.println("Shader ("+type2str+") compiled with error : " + err)

  def type2str = if (glType == GL_VERTEX_SHADER) "vertex"
                 else "fragment"
}

class VertexShader (source: String) extends Shader(source, GL_VERTEX_SHADER) {
}

class FragmentShader (source: String) extends Shader(source, GL_FRAGMENT_SHADER) {
}

class GLSLProgram (vShader: VertexShader, fShader: FragmentShader) {
  private val id = glCreateProgram()
  glAttachShader(id, vShader.id)
  glAttachShader(id, fShader.id)
  glLinkProgram(id)
  Renderer.checkGLError("Program linking")
  val err = glGetProgramInfoLog(id, 1024).trim()
  if (!err.equals(""))
    Console.println("Program compiled with error : " + err)

  private val uniformLocs = new HashMap[String, Int]
  private val attribLocs = new HashMap[String, Int]


  def bind () : Unit = glUseProgram(id)
  def unbind () {
    glUseProgram(0)
    for ((name, loc) <- attribLocs) {
      //TODO: not sure if this is really needed
      glDisableVertexAttribArray(loc)
    }
  }

  def getUniformLocation (name: String) : Int = {
    uniformLocs.getOrElseUpdate(name, {
      val loc = glGetUniformLocation(id, name)
      if (loc == -1) {
        Console.println("Error : uniform location not found for '"+name+"'")
      }
      loc
    })

  }

  def getAttribLocation (name: String) : Int = {
    attribLocs.getOrElseUpdate(name, {
      val loc = glGetAttribLocation(id, name)
      if (loc == -1) {
        Console.println("Error : attribute location not found for '"+name+"'")
      }
      loc
    })
  }

  def setAttribPointer (name: String, size: Int, normalize: Boolean, buff: FloatBuffer) {
    val loc = getAttribLocation(name)
    if (loc != -1) {
      glEnableVertexAttribArray(loc)
      glVertexAttribPointer(loc, size, normalize, 0, buff)
    }
  }

  def setUniform (name: String, v: Vector3) {
    val loc = getUniformLocation(name)
    if (loc != -1)
      glUniform3f(loc, v.x, v.y, v.z)
  }

  //Associate the given sampler to the given texture unit
  def setSamplerUnit (samplerName: String, unit: Int) = glUniform1i(getUniformLocation(samplerName), unit)
}

// vim: set ts=2 sw=2 et:
