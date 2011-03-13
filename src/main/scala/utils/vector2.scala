package utils

object Vector2 {
  def apply (x: Float, y:Float) = new Vector2(x,y)
}

class Vector2 (ax: Float, ay: Float) {
  var x:Float = ax
  var y:Float = ay

  def set (v: Vector2) = {x = v.x; y = v.y}
  
  def length () : Float = scala.math.sqrt(x*x + y*y).toFloat

  def normalize () = {
    val l = length();
    x /= l
    y /= l
  }

  def load (v: Vector2) {
    x = v.x
    y = v.y
  }
  
  def * (a: Float) : Vector2 = new Vector2(a*x, a*y)

  def / (a: Float) : Vector2 = new Vector2(x/a, y/a)

  def + (v: Vector2) : Vector2 = new Vector2(x+v.x, y+v.y)

  def - (v: Vector2) : Vector2 = new Vector2(x-v.x, y-v.y)

  def unary_-  : Vector2 = new Vector2(-x,-y)

  //dot product
  def * (v: Vector2) : Float = v.x*x + v.y*y

  def *= (a: Float) : Vector2 = {
    x *= a
    y *= a
    return this
  }

  def /= (a: Float) : Vector2 = this *= 1/a

  def += (v: Vector2) : Vector2 = {
    x += v.x
    y += v.y
    return this
  }

  def -= (v: Vector2) : Vector2 = {
    x -= v.x
    y -= v.y
    return this
  }

  def ~= (x: Float, y: Float) : Boolean = MathUtils.epsEq(this.x, x) && MathUtils.epsEq(this.y, y)

  def getOrientation (v: Vector2) = scala.math.signum(x*v.y - y*v.x)

  override def toString : String = new String("(" + x.toString + ", " + y.toString + ")")
}
