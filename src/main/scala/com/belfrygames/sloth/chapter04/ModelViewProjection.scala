package com.belfrygames.sloth.chapter04

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object ModelViewProjection {
  // Global view frustum class
  val viewFrustum = new GLFrustum

  // The shader manager
  val shaderManager = GLShaderManager

  // The torus
  val torusBatch = new GLTriangleBatch

  // Set up the viewport and the projection matrix
  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 1000.0f);
  }


  // Called to draw scene
  lazy val rotTimer = new CStopWatch
  val mTranslate = new M3DMatrix44f
  val mRotate = new M3DMatrix44f
  val mModelview = new M3DMatrix44f
  val mModelViewProjection = new M3DMatrix44f

  val vBlack = M3DVector(0.0f, 0.0f, 0.0f, 1.0f)

  def RenderScene() {
    // Set up time based animation
    var yRot = rotTimer.GetElapsedSeconds() * 60.0f;

    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Matrix Variables

    // Create a translation matrix to move the torus back and into sight
    m3dTranslationMatrix44(mTranslate, 0.0f, 0.0f, -2.5f);

    // Create a rotation matrix based on the current value of yRot
    m3dRotationMatrix44(mRotate, m3dDegToRad(yRot), 0.0f, 1.0f, 0.0f);

    // Add the rotation to the translation, store the result in mModelView
    m3dMatrixMultiply44(mModelview, mTranslate, mRotate);

    // Add the modelview matrix to the projection matrix,
    // the final matrix is the ModelViewProjection matrix.
    m3dMatrixMultiply44(mModelViewProjection, viewFrustum.GetProjectionMatrix(), mModelview);

    // Pass this completed matrix to the shader, and render the torus
    shaderManager.UseStockShader(GLT_SHADER_FLAT, mModelViewProjection, vBlack);
    torusBatch.Draw();


    // Swap buffers, and immediately refresh
    glutSwapBuffers();
    glutPostRedisplay();
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    // Black background
    glClearColor(0.8f, 0.8f, 0.8f, 1.0f);

    glEnable(GL_DEPTH_TEST);

    shaderManager.InitializeStockShaders();

    // This makes a torus
    gltMakeTorus(torusBatch, 0.4f, 0.15f, 30, 30);


    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
    glutInitWindowSize(800, 600);
    glutCreateWindow("ModelViewProjection Example");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

    //	GLenum err = glewInit();
    //	if (GLEW_OK != err) {
    //	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //	  return 1;
    //	}

    SetupRC();

    glutMainLoop();
  }
}
