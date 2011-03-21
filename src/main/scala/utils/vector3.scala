package utils

object Vector3 {
  def apply (x: Float, y:Float, z:Float) = new Vector3(x,y, z)
}

/* 3D vector */
class Vector3 (ax: Float, ay: Float, az: Float) {
  var x: Float = ax
  var y: Float = ay
  var z: Float = az

  /* additionnal constructors */
  def this () = this(0.0f,0.0f,0.0f)

  def copy (v: Vector3) : Vector3 = new Vector3(v.x, v.y, v.z)

  def length () : Float = scala.math.sqrt(x*x + y*y + z*z).toFloat

  def load (v: Vector3) {
    x = v.x
    y = v.y
    z = v.z
  }

  def normalize () : Unit =
    if (this.length() > 0) 
      this /= this.length() 

  def getNormalized () : Vector3 = this/this.length()

  def set (ax: Float, ay: Float, az: Float): Unit = {
    x = ax
    y = ay
    z = az
  }

  def projectionLength (other: Vector3) : Float =  other*this.getNormalized()

  def project (other: Vector3) : Vector3 = {
    val tmp = this.getNormalized()
    return tmp*(tmp*other)
  }

  def * (a: Float) : Vector3 = new Vector3(a*x, a*y, a*z)

  def / (a: Float) : Vector3 = new Vector3(x/a, y/a, z/a)

  def + (v: Vector3) : Vector3 = new Vector3(x+v.x, y+v.y, z+v.z)

  def - (v: Vector3) : Vector3 = new Vector3(x-v.x, y-v.y, z-v.z)

  def -  : Vector3 = new Vector3(-x,-y,-z)

  //dot product
  def * (v: Vector3) : Float = v.x*x + v.y*y + v.z*z

  //cross product
  def % (v: Vector3) : Vector3 = new Vector3(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x)

  def *= (a: Float) : Vector3 = {
    x *= a
    y *= a
    z *= a
    return this
  }

  def /= (a: Float) : Vector3 = this *= 1/a

  def += (v: Vector3) : Vector3 = {
    x += v.x
    y += v.y
    z += v.z
    return this
  }

  def -= (v: Vector3) : Vector3 = this+(v*(-1))

  //Epsilon equals
  //def ~= (v: Vector3) : Boolean = (x ~= v.x) && (y ~= v.y) && (z ~= v.z)
  def ~= (v: Vector3) : Boolean = MathUtils.epsEq(x, v.x) && MathUtils.epsEq(y, v.y) && MathUtils.epsEq(z, v.z)

  override def toString : String = new String("(" + x.toString + ", " + y.toString + ", " + z.toString + ")")
}
