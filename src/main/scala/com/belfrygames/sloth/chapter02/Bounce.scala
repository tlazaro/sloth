package com.belfrygames.sloth.chapter02

import com.belfrygames.sloth.GLBatch
import com.belfrygames.sloth.GLShaderManager
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.GLT_STOCK_SHADER._
import com.belfrygames.sloth.GLT_SHADER_ATTRIBUTE._

import org.lwjgl.opengl.GL11._
import org.lwjgl.util.vector.Vector4f

object Bounce {
  // Bounce.cpp
// Bounce a Block around the screen

  val	squareBatch = new GLBatch
  val	shaderManager = GLShaderManager


  var blockSize = 0.1f;
  val vVerts = Array( -blockSize - 0.5f, -blockSize, 0.0f,
					 blockSize - 0.5f, -blockSize, 0.0f,
					 blockSize - 0.5f,  blockSize, 0.0f,
					 -blockSize - 0.5f,  blockSize, 0.0f)

///////////////////////////////////////////////////////////////////////////////
// This function does any needed initialization on the rendering context.
// This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
	// Black background
	glClearColor(0.0f, 0.0f, 1.0f, 1.0f );

	shaderManager.InitializeStockShaders();

	// Load up a triangle
	squareBatch.Begin(GL_TRIANGLE_FAN, 4);
	squareBatch.CopyVertexData3f(vVerts);
	squareBatch.End();
  }

// Respond to arrow keys by moving the camera frame of reference
  var xDir = 1.0f;
  var yDir = 1.0f;
  def BounceFunction() {
	val stepSize = 0.005f;

	var blockX = vVerts(0);   // Upper left X
	var blockY = vVerts(7);  // Upper left Y

	blockY += stepSize * yDir;
	blockX += stepSize * xDir;

	// Collision detection
	if(blockX < -1.0f) { blockX = -1.0f; xDir *= -1.0f; }
	if(blockX > (1.0f - blockSize * 2)) { blockX = 1.0f - blockSize * 2; xDir *= -1.0f; }
	if(blockY < -1.0f + blockSize * 2)  { blockY = -1.0f + blockSize * 2; yDir *= -1.0f; }
	if(blockY > 1.0f) { blockY = 1.0f; yDir *= -1.0f; }

	// Recalculate vertex positions
	vVerts(0) = blockX;
	vVerts(1) = blockY - blockSize*2;

	vVerts(3) = blockX + blockSize*2;
	vVerts(4) = blockY - blockSize*2;

	vVerts(6) = blockX + blockSize*2;
	vVerts(7) = blockY;

	vVerts(9) = blockX;
	vVerts(10) = blockY;

	squareBatch.CopyVertexData3f(vVerts);
  }





///////////////////////////////////////////////////////////////////////////////
// Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	val vRed = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f);
	shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vRed);
	squareBatch.Draw();

	// Flush drawing commands
	glutSwapBuffers();

	BounceFunction();
	glutPostRedisplay(); // Redraw
  }



///////////////////////////////////////////////////////////////////////////////
// Window has changed size, or has just been created. In either case, we need
// to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
	glViewport(0, 0, w, h);
  }

///////////////////////////////////////////////////////////////////////////////
// Main entry point for GLUT based programs
  def main(argv : Array[String]) : Unit = {
	if (argv.size > 0)
	  gltSetWorkingDirectory(argv(0))

	glutInit(argv);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Bouncing Block");

//	GLenum err = glewInit();
//	if (GLEW_OK != err)
//	{
//	  // Problem: glewInit failed, something is seriously wrong.
//	  fprintf(stderr, "Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	glutReshapeFunc(ChangeSize);
	glutDisplayFunc(RenderScene);

	SetupRC();

	glutMainLoop();
	return 0;
  }
}
