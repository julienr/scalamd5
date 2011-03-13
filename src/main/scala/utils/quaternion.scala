package utils
import java.nio._

class Quaternion(ar: Float, ax: Float, ay: Float, az: Float) {
  var r = ar
  var x = ax
  var y = ay
  var z = az

  /* additionnal constructors */
  def this () = this(0.0f, 0.0f,0.0f,0.0f)
  def copy (q: Quaternion) : Quaternion = new Quaternion(q.r, q.x, q.y, q.z)
  def this (angle: Float, axis: Vector3) = { this(); setRotation(angle, axis); }

  def + (q: Quaternion) : Quaternion = new Quaternion(r+q.r, x+q.x, y+q.y, z+q.z)
  def - (q: Quaternion) : Quaternion = new Quaternion(r-q.r, x-q.x, y-q.y, z-q.z)
  def * (q: Quaternion) : Quaternion = {
    new Quaternion(r*q.r - x*q.x - y*q.y - z*q.z, 
                   r*q.x + x*q.r + y*q.z - z*q.y,
                   r*q.y + y*q.r + z*q.x - x*q.z,
                   r*q.z + z*q.r + x*q.y - y*q.x)
  }

  def * (f: Float) : Quaternion = new Quaternion(r*f, x*f, y*f, z*f)
  def / (f: Float) : Quaternion = this*1/f
  def / (q: Quaternion) : Quaternion = this.*(q.inverse);

  def += (q: Quaternion) : Quaternion = {
    r += q.r
    x += q.x
    y += q.y
    z += q.z
    return this
  }

  def -= (q: Quaternion) : Quaternion = {
    r -= q.r
    x -= q.x
    y -= q.y
    z -= q.z
    return this
  }

  def *= (q: Quaternion) : Quaternion = {
    r = r*q.r - x*q.x - y*q.y - z*q.z
    x = r*q.x + x*q.r + y*q.z - z*q.y
    y = r*q.y + y*q.r + z*q.x - x*q.z
    z = r*q.z + z*q.r + x*q.y - y*q.x
    return this
  }

  def *= (f: Float) : Quaternion = {
    r *= f
    x *= f
    y *= f
    z *= f
    return this
  }

  def /= (f: Float) : Quaternion = this*=1/f

  def /= (q: Quaternion) : Quaternion = this*=q.inverse

  def inverse : Quaternion = getConjugate/magnitude
  def getConjugate : Quaternion = new Quaternion(r, -x, -y, -z)
  
  def conjugate = {
    x = -x
    y = -y
    z = -z
  }

  def unit : Quaternion = this/magnitude

  def magnitude : Float = scala.math.sqrt(r*r + x*x + y*y + z*z).toFloat

  def setRotation (angle: Float, axis: Vector3) : Unit = {
    val tmp : Vector3 = axis.getNormalized()
    val sin_a : Float = scala.math.sin(angle/2).toFloat
    val cos_a : Float = scala.math.cos(angle/2).toFloat
    x = tmp.x*sin_a
    y = tmp.y*sin_a
    z = tmp.z*sin_a
    r = cos_a
  }

  def normalize : Unit = this /= magnitude

  def getNormalized : Quaternion = this / magnitude

  def rotate (v: Vector3) : Vector3 = {
    val qvec : Vector3 = new Vector3(x,y,z)
    val uv : Vector3 = qvec%v
    val uuv : Vector3 = qvec%uv
    uv *= (2.0f*r)
    uuv *= 2.0f
    return v+uv+uuv
  }

  def xAxis : Vector3 = {
    val ty = 2.0f*y
    val tz = 2.0f*z
    val twy = ty*r
    val twz = tz*r
    val txy = ty*x
    val txz = tz*x
    val tyy = ty*y
    val tzz = tz*z

    return new Vector3(1.0f - (tyy+tzz), txy+twz, txz-twy)
  }

  def yAxis : Vector3 = {
    val tx = 2*x
    val ty = 2*y
    val tz = 2*z
    val twx = tx*r
    val twz = tz*r
    val txx = tx*x
    val txy = ty*x
    val tyz = tz*y
    val tzz = tz*z

    return new Vector3(txy-twz, 1.0f-(txx+tzz), tyz+twx)
  }

  def zAxis : Vector3 = {
    val tx = 2*x
    val ty = 2*y
    val tz = 2*z
    val twx = tx*r
    val twy = ty*r
    val txx = tx*x
    val txz = tz*x
    val tyy = ty*y
    val tyz = tz*y

    return new Vector3(txz+twy, tyz-twx, 1.0f-(txx+tyy));
  }

  override def toString : String = {
    val v = new Vector3(x,y,z)
      return "["+v+","+r+"]"
  }

  //return the 4*4 rotation matrix that corresponds to this quaternion
  //returns it in column-major format
  def getMatrix : Matrix4 = {
    val m = new Matrix4()
    m.set(0, 0, 1.0f - 2.0f*(y*y+z*z))
    m.set(1, 0, 2.0f*(x*y+z*r))
    m.set(2, 0, 2.0f*(x*z-y*r))
    m.set(3, 0, 0.0f)

    m.set(0, 1, 2.0f*(x*y-z*r))
    m.set(1, 1, 1.0f - 2.0f*(x*x+z*z))
    m.set(2, 1, 2.0f*(z*y+x*r))
    m.set(3, 1, 0.0f)

    m.set(0, 2, 2.0f*(x*z+y*r))
    m.set(1, 2, 2.0f*(y*z-x*r))
    m.set(2, 2, 1.0f - 2.0f*(x*x+y*y))
    m.set(3, 2, 0.0f)

    m.set(0, 3, 0.0f)
    m.set(1, 3, 0.0f)
    m.set(2, 3, 0.0f)
    m.set(3, 3, 1.0f)
    m
  }

}
