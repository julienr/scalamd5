package test.scala.utils
import utils._
import org.scalatest.Spec
import org.scalatest.matchers.MustMatchers
import scala.math.cos
import scala.math.sin
import java.nio.FloatBuffer

class QuaternionSpec extends Spec with MustMatchers {
  def eq(x:Float, y:Float) = MathUtils.epsEq(x,y)

/*  def verifyMat(m: Matrix4, vals: List[Float]) = {
    for (i <- Range(0,16))
      assert(eq(m.get(i),vals(i)), "m.get("+i+")="+m.get(i)+", vals("+i+")="+vals(i))
  }*/

  def sinf (f: Float) = sin(f).toFloat
  def cosf (f: Float) = cos(f).toFloat

  describe("A Quaternion") {
    it("should convert to a correct rotation matrix around x") {
      val angle = 60.0f
      val q = new Quaternion(angle, Vector3(1,0,0))
      //matrix is column-major
     /* verifyMat(q.getMatrix, List(1,0,0,0,
                                  0,cosf(angle),sinf(angle),0,
                                  0,-sinf(angle),cosf(angle),0,
                                  0,0,0,1))*/
    }
  }
}

// vim: set ts=2 sw=2 et:
