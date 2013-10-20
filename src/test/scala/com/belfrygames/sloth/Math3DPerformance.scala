package com.belfrygames.sloth

import org.scalameter.api._
import org.scalameter.reporting.LoggingReporter
import com.belfrygames.sloth.Math3D.M3DMatrix44f

object Math3DPerformance extends PerformanceTest {

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

  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val cameraFrame = new GLFrame
  val mCamera = new M3DMatrix44f;

  /* tests */

  performance of "Math3DPerformance" in {
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