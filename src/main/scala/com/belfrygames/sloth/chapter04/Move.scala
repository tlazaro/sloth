package com.belfrygames.sloth.chapter04

import com.belfrygames.sloth.Math3D.M3DVector
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth._
import com.belfrygames.sloth.GLT_STOCK_SHADER._
import com.belfrygames.sloth.GLT_SHADER_ATTRIBUTE._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object Move {
  val squareBatch = new GLBatch

  val shaderManager = GLShaderManager

  val blockSize = 0.1f;
  val vVerts = Array(-blockSize, -blockSize, 0.0f, 
					 blockSize, -blockSize, 0.0f,
					 blockSize,  blockSize, 0.0f,
					 -blockSize,  blockSize, 0.0f)

  var xPos = 0.0f;
  var yPos = 0.0f;

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
  def SpecialKeys(key : Int, x : Int, y : Int) {
	val stepSize = 0.025f;

	if(key == GLUT_KEY_UP)
	  yPos += stepSize;

	if(key == GLUT_KEY_DOWN)
	  yPos -= stepSize;

	if(key == GLUT_KEY_LEFT)
	  xPos -= stepSize;

	if(key == GLUT_KEY_RIGHT)
	  xPos += stepSize;

	// Collision detection
	if(xPos < (-1.0f + blockSize)) xPos = -1.0f + blockSize;

	if(xPos > (1.0f - blockSize)) xPos = 1.0f - blockSize;

    if(yPos < (-1.0f + blockSize))  yPos = -1.0f + blockSize;

	if(yPos > (1.0f - blockSize)) yPos = 1.0f - blockSize;

	glutPostRedisplay();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  var yRot = 0.0f
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	val vRed = M3DVector(1.0f, 0.0f, 0.0f, 1.0f)

    val mFinalTransform = new M3DMatrix44f
	val mTranslationMatrix = new M3DMatrix44f
	val mRotationMatrix = new M3DMatrix44f

    // Just Translate
    m3dTranslationMatrix44(mTranslationMatrix, xPos, yPos, 0.0f);

    // Rotate 5 degrees evertyime we redraw
    yRot += 5.0f;
    m3dRotationMatrix44(mRotationMatrix, m3dDegToRad(yRot), 0.0f, 0.0f, 1.0f);

    m3dMatrixMultiply44(mFinalTransform, mTranslationMatrix, mRotationMatrix);


	shaderManager.UseStockShader(GLT_SHADER_FLAT, mFinalTransform, vRed);
	squareBatch.Draw();

	// Perform the buffer swap
	glutSwapBuffers();
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
	if (argv.size > 0) gltSetWorkingDirectory(argv(0))

	glutInit(argv);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
	glutInitWindowSize(600, 600);
	glutCreateWindow("Move Block with Arrow Keys");

//	GLenum err = glewInit();
//	if (GLEW_OK != err)
//	{
//	  // Problem: glewInit failed, something is seriously wrong.
//	  fprintf(stderr, "Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	glutReshapeFunc(ChangeSize);
	glutDisplayFunc(RenderScene);
    glutSpecialFunc(SpecialKeys);

	SetupRC();

	glutMainLoop();
	return 0;
  }
}
