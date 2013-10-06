package com.belfrygames.sloth.chapter03

import com.belfrygames.sloth._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object Scissor {
  ///////////////////////////////////////////////////////////
  // Called to draw scene
  def RenderScene() {
    // Clear blue window
    glClearColor(0.0f, 0.0f, 1.0f, 0.0f)
    glClear(GL_COLOR_BUFFER_BIT)

    // Now set scissor to smaller red sub region
    glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
    glScissor(100, 100, 600, 400)
    glEnable(GL_SCISSOR_TEST)
    glClear(GL_COLOR_BUFFER_BIT)

    // Finally, an even smaller green rectangle
    glClearColor(0.0f, 1.0f, 0.0f, 0.0f)
    glScissor(200, 200, 400, 200)
    glClear(GL_COLOR_BUFFER_BIT)

    // Turn scissor back off for next render
    glDisable(GL_SCISSOR_TEST)

    glutSwapBuffers()
  }


  ///////////////////////////////////////////////////////////
  // Set viewport and projection
  def ChangeSize(w: Int, h: Int) {
    // Prevent a divide by zero
    // Set Viewport to window dimensions
    glViewport(0, 0, w, if (h == 0) 1 else h)
  }

  ///////////////////////////////////////////////////////////
  // Program entry point
  def main(argv: Array[String]): Unit = {
    glutInit(argv)
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB)
    glutInitWindowSize(800, 600)
    glutCreateWindow("OpenGL Scissor")
    glutReshapeFunc(ChangeSize)
    glutDisplayFunc(RenderScene)
    glutMainLoop()
  }
}
