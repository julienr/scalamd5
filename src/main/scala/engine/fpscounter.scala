package engine

object FPSCounter {
  private var acc : Float = 0.0f
  private var numFrames : Int = 0

  private var fps : Float = 0.0f

  def countFrame (elapsedS : Float) {
    acc += elapsedS
    numFrames += 1
    if (acc >= 1.0f) {
      fps = numFrames
      acc = 0.0f
      numFrames = 0
      Console.println("FPS : " + fps)
    }
  }
}

// vim: set ts=2 sw=2 et:
