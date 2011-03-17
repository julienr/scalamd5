package engine
import org.lwjgl.opengl.GL20._

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


  def bind () : Unit = glUseProgram(id)
  def unbind (): Unit = glUseProgram(0)

  def getUniformLocation (name: String) : Int = glGetUniformLocation(id, name)

  //Associate the given sampler to the given texture unit
  def setSamplerUnit (samplerName: String, unit: Int) = glUniform1i(getUniformLocation(samplerName), unit)
}

// vim: set ts=2 sw=2 et:
