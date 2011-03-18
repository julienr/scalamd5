package md5viewer
import utils._
import scala.collection.mutable.MutableList
import java.nio._
import org.lwjgl._
import engine._

//Anim

protected class JointInfo (val parent: Int, val flags: Int, val frameIndex: Int) {
}

protected class JointBaseFrame (val position: Vector3, val rotation: Quaternion) {
}

protected class BaseFrame (val joints: List[JointBaseFrame]) {
}

protected class Frame (val values: Array[Float]) {
}

class MD5Anim (val version: Int, val commandLine: String, val jointInfos: List[JointInfo], val baseframe : BaseFrame, val frames: List[Frame], frameRate: Int) {
  var currentTime = 0.0f
  var previousTime = 0.0f

  var currentFrame = 0

  def animate (model: MD5Model, elapsedTime: Float) {
    currentTime += elapsedTime
    if ((currentTime-previousTime) > 1/frameRate.toFloat) {
      previousTime = currentTime
      currentFrame += 1
      if (currentFrame >= frames.length) {
        currentFrame = 0
      }
      for (joint <- model.baseJoints) {
        animateJoint(joint, currentFrame, Quaternion(0,Vector3(0,1,0)), Vector3(0,0,0))
      }
      model.skin()
    }
  }

  def animateJoint (joint: Joint, frame: Int, parentRotation: Quaternion, parentPosition: Vector3) {
    val animatedPosition = baseframe.joints(joint.number).position
    val animatedRotation = baseframe.joints(joint.number).rotation

    val flags = jointInfos(joint.number).flags
    val baseIdx = jointInfos(joint.number).frameIndex
    var n = 0
    if ((flags & 1) == 1) { //Tx
      animatedPosition.x = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 2) == 2) { //Ty
      animatedPosition.y = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 4) == 4) { //Tz
      animatedPosition.z = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 8) == 8) { //Qx
      animatedRotation.x = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 16) == 16) { //Qy
      animatedRotation.y = frames(frame).values(baseIdx+n)
      n += 1
    }
    if ((flags & 32) == 32) { //Qz
      animatedRotation.z = frames(frame).values(baseIdx+n)
      n += 1
    }
    animatedRotation.computeR()

    if (joint.parentJoint == null) { //base joint
      joint.position = animatedPosition
      joint.rotation = animatedRotation
    } else { //has a parent
      joint.position = parentRotation.rotate(animatedPosition) + parentPosition
      joint.rotation = parentRotation*animatedRotation
    }

    for (child <- joint.children) {
      animateJoint(child, frame, joint.rotation, joint.position)
    }
  }
}
// vim: set ts=2 sw=2 et:
