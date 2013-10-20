package com.belfrygames.sloth

import org.scalameter.api._
import com.belfrygames.sloth.exp.memory.unsafe.{M3DMatrix44f, UnsafeGLFrame, UnsafeGLMatrixStack}

object UnsafeMath3DPerformance extends PerformanceTest {

  /* configuration */

  lazy val executor = LocalExecutor(
    new Executor.Warmer.Default,
    Aggregator.min,
    new Measurer.IgnoringGC)
  lazy val reporter = new LoggingReporter
  lazy val persistor = Persistor.None

  /* inputs */

  val sizes = Gen.range("size")(300000, 1500000, 300000)

  val ranges = for {
    size <- sizes
  } yield 0 until size

  val modelViewMatrix = new UnsafeGLMatrixStack
  val projectionMatrix = new UnsafeGLMatrixStack
  val cameraFrame = new UnsafeGLFrame
  val mCamera = M3DMatrix44f()

  /* tests */

  performance of "UnsafeMath3DPerformance" in {
    measure method "matrix" in {
      using(ranges) in {
        r =>
          modelViewMatrix.PushMatrix()
          cameraFrame.GetCameraMatrix(mCamera)
          modelViewMatrix.MultMatrix(mCamera)

          // Draw the world upside down
          modelViewMatrix.PushMatrix()
          modelViewMatrix.Scale(1.0f, -1.0f, 1.0f) // Flips the Y Axis
          modelViewMatrix.Translate(0.0f, 0.8f, 0.0f) // Scootch the world down a bit...
          modelViewMatrix.PopMatrix()
          modelViewMatrix.PopMatrix()
      }
    }
  }
}