package com.belfrygames.sloth.chapter06

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.Math3D.M3DVector._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL32._

object FlatShader {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val torusBatch = new GLTriangleBatch
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform

  var flatShader = 0;
  // The Flat shader

  var locMVP = 0;
  // The location of the ModelViewProjection matrix uniform
  var locColor = 0;

  // The location of the color value uniform

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    // Background
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    glEnable(GL_DEPTH_TEST);
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

    viewFrame.MoveForward(4.0f);

    // Make the torus
    gltMakeTorus(torusBatch, .80f, 0.25f, 52, 26);

    flatShader = gltLoadShaderPairWithAttributes("FlatShader.vp", "FlatShader.fp", 1, GLT_ATTRIBUTE_VERTEX, "vVertex");

    locMVP = glGetUniformLocation(flatShader, "mvpMatrix");
    locColor = glGetUniformLocation(flatShader, "vColorValue");
  }

  // Cleanup
  def ShutdownRC() {

  }


  // Called to draw scene
  lazy val rotTimer = new CStopWatch
  val vColor = M3DVector(0.1f, 0.1f, 1.0f, 1.0f);

  def RenderScene() {

    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    modelViewMatrix.PushMatrix(viewFrame);
    modelViewMatrix.Rotate(rotTimer.GetElapsedSeconds() * 10.0f, 0.0f, 1.0f, 0.0f);


    glUseProgram(flatShader);
    glUniform4(locColor, vColor);
    glUniformMatrix4(locMVP, false, transformPipeline.GetModelViewProjectionMatrix());
    torusBatch.Draw();

    modelViewMatrix.PopMatrix();


    glutSwapBuffers();
    glutPostRedisplay();
  }


  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 100.0f);

    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Simple Transformed Geometry");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

    //	GLenum err = glewInit();
    //	if (GLEW_OK != err) {
    //	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //	  return 1;
    //    }

    SetupRC();
    glutMainLoop();
    ShutdownRC();
  }
}
