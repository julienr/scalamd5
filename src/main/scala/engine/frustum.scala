package engine
import utils._

/**
  Implements a frustum: a closed volume defined by a near plane, a far plane
  and a number n > 3 of sides planes
  All planes normals MUST poInt INSIDE the frustum
  */
class Frustum (position: Vector3, near: Plane, far: Plane, planes: List[Plane]) {
    if (planes.length < 3) throw new Error("Frustum must have at least 3 side planes")
    
    var origin = position
    var nearPlane : Plane = near
    var farPlane : Plane = far
    var sidePlanes = List[Plane]()
    
    def addPlane (p: Plane) : Unit = { sidePlanes = p :: sidePlanes }
    
    def clipPolygon (polyList: List[Vector3]): List[Vector3] = {
      var list = polyList
      for (p <- sidePlanes) yield {
        list = p.clipPolygon(list)
      }
      return list
    }
}
