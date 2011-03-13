package utils
import java.nio.FloatBuffer

class Matrix4 {
  //stores data in column-major order
  private val data = FloatBuffer.allocate(16)

  def set (row: Int, col: Int, v: Float) = data.put(col*4+row, v)
  def get (row: Int, col: Int) = data.get(col*4+row)

  //returns a float buffer containing the elements of this matrix. DO NOT modify
  //this buffer directly as this might impact the matrix
  def getFloatBuffer () = data
}

// vim: set ts=2 sw=2 et:
