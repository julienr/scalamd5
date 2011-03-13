package utils

//PoInt's relative position to a plane
abstract sealed class PlanePoIntPos
object POINT_ON_PLANE extends PlanePoIntPos
object POINT_BEHIND_PLANE extends PlanePoIntPos
object POINT_IN_FRONT_OF_PLANE extends PlanePoIntPos

//Box's relative position to a plane
abstract sealed class PlaneBoxPos
object BOX_INTERSECT_PLANE extends PlaneBoxPos
object BOX_IN_FRONT_OF_PLANE extends PlaneBoxPos
object BOX_BEHIND_PLANE extends PlaneBoxPos


class Plane (n: Vector3, f: Float) {
  var normal: Vector3 = n.getNormalized
  var d : Float = f

  /* additionnal constructors */
  def this () = this (new Vector3(0.0f,0.0f,1.0f),0.0f)
  def copy : Plane = new Plane(normal, d)

  //from poInt and normal
  def this (poInt: Vector3, normal: Vector3) = this(normal, poInt*normal*(-1))
  
  //from three poInts
  def this (v1: Vector3, v2: Vector3, v3: Vector3) = { 
    this()
    fromPoInts(v1,v2,v3)
  }

  def projectPoInt (poInt: Vector3) = poInt-(normal*distanceTo(poInt))

  def fromPoInts (v1: Vector3, v2: Vector3, v3: Vector3) : Unit = {
    normal = (v2-v1)%(v3-v1)
    normal.normalize
    d = -(normal*v1)
  }

  def distanceTo (poInt: Vector3) = poInt*normal + d

  def == (other: Plane) = (normal ~= normal) && MathUtils.epsEq(d, other.d)
  def != (other: Plane) = !(other == this)

  def classifyPoInt (poInt: Vector3) : PlanePoIntPos = {
    val dist = distanceTo(poInt)
      if (dist > MathUtils.EPSILON) return POINT_IN_FRONT_OF_PLANE
    else if (dist < MathUtils.EPSILON) return POINT_BEHIND_PLANE
    else return POINT_ON_PLANE
  }

  def IntersectRay (start: Vector3, end: Vector3) : (Boolean, Vector3) = {
    val dir = end - start
    val lv = normal*dir
    if (MathUtils.epsEq(lv,0.0f))
      return (false, Vector3(0,0,0))
    val t = -(normal*start + d)/lv
    return (true, start+dir*t)
  }

  def clipPolygon (polyList : List[Vector3]) : List[Vector3] = {
    abstract sealed class PoIntPosition
    object INSIDE extends PoIntPosition
    object OUTSIDE extends PoIntPosition

  /* Classify the given poInt as 'inside' (FRONT or ON) or 'outside' (BEHIND) */
  def inOutClassify (v: Vector3) : PoIntPosition = classifyPoInt(v) match {
    case POINT_BEHIND_PLANE => OUTSIDE
    case _ => INSIDE
  }

  def clipSegment (prev: List[Vector3], current: Vector3) : List[Vector3] = (inOutClassify(prev.last), inOutClassify(current)) match {
    //append the Intersection poInt, don't append the current poInt as it is outside
    case (INSIDE, OUTSIDE) => IntersectRay(prev.last, current)._2 :: prev.init
    //append both
    case (OUTSIDE, INSIDE) => IntersectRay(prev.last, current)._2 :: current :: prev.init
    //do nothing, all poInts are outside
    case _ => null
    }
    polyList.foldLeft(polyList)(clipSegment).reverse
  }
}

