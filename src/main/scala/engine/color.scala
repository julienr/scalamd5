package utils

object COL_BLACK extends Color(0.0f,0.0f,0.0f,0.0f)
object COL_WHITE extends Color(1.0f,1.0f,1.0f,1.0f)
object COL_RED extends Color(1.0f,0.0f,0.0f,1.0f)
object COL_GREEN extends Color(0.0f,1.0f,0.0f,1.0f)
object COL_BLUE extends Color(0.0f,0.0f,1.0f,1.0f)

class Color (ar: Float, ag: Float, ab: Float, aa: Float) {
  var r = ar
  var g = ag
  var b = ab
  var a = aa
}
