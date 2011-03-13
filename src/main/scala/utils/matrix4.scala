package utils
import java.nio.FloatBuffer

//A matrix backed by a FloatBuffer, to be used with OpenGL
class Matrix4 {
  //stores data in column-major order
  private val data = FloatBuffer.allocate(16)

  //returns a float buffer containing the elements of this matrix. DO NOT modify
  //this buffer directly as this might impact the matrix
  def getFloatBuffer () = data


  def update(row: Int, col: Int, v: Float) = data.put(col*4+row, v)
  def apply (row: Int, col: Int) = data.get(col*4+row)

  override def toString () : String = {
    var s = "\n"

    for (i <- Range(0,4)) {
      s += "["
      for (j <- Range(0,4)) {
        s += ""+this(i,j)+" "
      }
      s+= "]\n"
    }
    s
  }

}

// vim: set ts=2 sw=2 et:
