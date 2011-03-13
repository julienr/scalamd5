package utils

//General math definitions and utilities
object MathUtils {
  val PI : Float = 3.141593f
  val PI_2 : Float = PI/2.0f
  val EPSILON : Float = 0.0001f
  val DEG_TO_RAD : Float = PI/180.0f
  val RAD_TO_DEG : Float = 180.0f/PI
  val INFINITY: Float = java.lang.Float.MAX_VALUE

//  implicit def ~= (x: Float, y: Float) : Boolean = abs(x-y) < EPSILON
  def epsEq (x: Float, y: Float) : Boolean = scala.math.abs(x-y) < EPSILON

  //Clamp v in the Intervall [min, max]
  def clamp (v: Float, min: Float, max: Float) = {
    if (v < min) min
    else if (v > max) max
    else v
  }
}

