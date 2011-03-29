// Move.scala
// Move a Block based on arrow key movements
package com.belfrygames.sloth.chapter02

import com.belfrygames.sloth.GLShaderManager
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLT_STOCK_SHADER._
import com.belfrygames.sloth.GLT_SHADER_ATTRIBUTE._
import com.belfrygames.sloth.GLBatch
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.STD._
import com.belfrygames.sloth.glut.GLUT._

import org.lwjgl.opengl.GL11._
import org.lwjgl.util.vector.Vector4f

object Move {
  val squareBatch = new GLBatch
  val shaderManager = GLShaderManager

  val blockSize = 0.1f
  val vVerts = Array(-blockSize, -blockSize, 0.0f,
					 blockSize, -blockSize, 0.0f,
					 blockSize,  blockSize, 0.0f,
					 -blockSize,  blockSize, 0.0f)

  ///////////////////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering context.
  // This is the first opportunity to do any OpenGL related tasks.
  def SetupRC()	{
	// Black background
	glClearColor(0.0f, 0.0f, 1.0f, 1.0f)

	shaderManager.InitializeStockShaders()

	// Load up a triangle
	squareBatch.Begin(GL_TRIANGLE_FAN, 4)
	squareBatch.CopyVertexData3f(vVerts)
	squareBatch.End()
  }

  // Respond to arrow keys by moving the camera frame of reference
  def SpecialKeys(key : Int, x : Int, y : Int) {
	val stepSize = 0.025f;

	var blockX = vVerts(0);   // Upper left X
	var blockY = vVerts(7);  // Upper left Y

	if(key == GLUT_KEY_UP)
	  blockY += stepSize;

	if(key == GLUT_KEY_DOWN)
	  blockY -= stepSize;

	if(key == GLUT_KEY_LEFT)
	  blockX -= stepSize;

	if(key == GLUT_KEY_RIGHT)
	  blockX += stepSize;

	// Collision detection
	if(blockX < -1.0f) blockX = -1.0f;
	if(blockX > (1.0f - blockSize * 2)) blockX = 1.0f - blockSize * 2;;
	if(blockY < -1.0f + blockSize * 2)  blockY = -1.0f + blockSize * 2;
	if(blockY > 1.0f) blockY = 1.0f;

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

	glutPostRedisplay()
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	val vRed = new Vector4f(1.0f, 0.0f, 0.0f, 1.0f );
	shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vRed);
	squareBatch.Draw();

	// Flush drawing commands
	glutSwapBuffers();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int)  {
	glViewport(0, 0, w, h);
  }

  ///////////////////////////////////////////////////////////////////////////////
 // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0)
	  gltSetWorkingDirectory(args(0))

	glutInit(args)
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Move Block with Arrow Keys");

//	GLenum err = glewInit();
//	if (GLEW_OK != err)
//	{
//	  // Problem: glewInit failed, something is seriously wrong.
//	  fprintf(stderr, "Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	glutReshapeFunc(ChangeSize)
	glutDisplayFunc(RenderScene)
	glutSpecialFunc(SpecialKeys)

	SetupRC()

	glutMainLoop()
  }
}