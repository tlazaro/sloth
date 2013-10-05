package com.belfrygames.sloth.chapter04

import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object SphereWorld {
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val viewFrustum = new GLFrustum
  val transformPipeline = new GLGeometryTransform

  val torusBatch = new GLTriangleBatch
  val floorBatch = new GLBatch

  //////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
	// Initialze Shader Manager
	shaderManager.InitializeStockShaders();

	glEnable(GL_DEPTH_TEST);
	glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

	// This makes a torus
	gltMakeTorus(torusBatch, 0.4f, 0.15f, 30, 30);


	floorBatch.Begin(GL_LINES, 324);
	var x = -20.0f
    while(x <= 20.0f) {
	  floorBatch.Vertex3f(x, -0.55f, 20.0f);
	  floorBatch.Vertex3f(x, -0.55f, -20.0f);

	  floorBatch.Vertex3f(20.0f, -0.55f, x);
	  floorBatch.Vertex3f(-20.0f, -0.55f, x);
	  x += 0.5f
	}
    floorBatch.End();
  }


///////////////////////////////////////////////////
// Screen changes size or is initialized
  def ChangeSize(nWidth : Int, nHeight : Int) {
	glViewport(0, 0, nWidth, nHeight);

    // Create the projection matrix, and load it on the projection matrix stack
	viewFrustum.SetPerspective(35.0f, nWidth.toFloat / nHeight.toFloat, 1.0f, 100.0f);
	projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());

    // Set the transformation pipeline to use the two matrix stacks
	transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);
  }


  // Called to draw scene
  // Color values
  val vFloorColor = M3DVector(0.0f, 1.0f, 0.0f, 1.0f);
  val vTorusColor = M3DVector(1.0f, 0.0f, 0.0f, 1.0f);
  lazy val rotTimer = new CStopWatch // Made lazy so that it gets initialized in RenderScene when needed and not on class creation

  def RenderScene() {
    // Time Based animation
	var yRot = rotTimer.GetElapsedSeconds() * 60.0f;

	// Clear the color and depth buffers
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Save the current modelview matrix (the identity matrix)
	modelViewMatrix.PushMatrix();

	// Draw the ground
	shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vFloorColor);
	floorBatch.Draw();

    // Draw the spinning Torus
    modelViewMatrix.Translate(0.0f, 0.0f, -2.5f);
    modelViewMatrix.Rotate(yRot, 0.0f, 1.0f, 0.0f);
    shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vTorusColor);
    torusBatch.Draw();

	// Restore the previous modleview matrix (the idenity matrix)
	modelViewMatrix.PopMatrix();

    // Do the buffer Swap
    glutSwapBuffers();

    // Tell GLUT to do it again
    glutPostRedisplay();
  }


  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(800,600);

    glutCreateWindow("OpenGL SphereWorld");

    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

//    GLenum err = glewInit();
//    if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}


    SetupRC();
    glutMainLoop();
  }
}
