// Blending.scala
package com.belfrygames.sloth.chapter03

import com.belfrygames.sloth._
import com.belfrygames.sloth.GLT_STOCK_SHADER._
import com.belfrygames.sloth.GLT_SHADER_ATTRIBUTE._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.util.vector.Vector4f

// Move a Block based on arrow key movements,
// Blend it with background blocks
object Blending {

  val squareBatch = new GLBatch
  val greenBatch = new GLBatch
  val redBatch = new GLBatch
  val blueBatch = new GLBatch
  val blackBatch = new GLBatch

  val shaderManager = GLShaderManager

  val blockSize = 0.2f;
  val vVerts = Array( -blockSize, -blockSize, 0.0f,
					 blockSize, -blockSize, 0.0f,
					 blockSize,  blockSize, 0.0f,
					 -blockSize,  blockSize, 0.0f)

///////////////////////////////////////////////////////////////////////////////
// This function does any needed initialization on the rendering context.
// This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
	// Black background
	glClearColor(1.0f, 1.0f, 1.0f, 1.0f )

	shaderManager.InitializeStockShaders()

	// Load up a triangle fan
	squareBatch.Begin(GL_TRIANGLE_FAN, 4)
	squareBatch.CopyVertexData3f(vVerts)
	squareBatch.End()

    val vBlock = Array( 0.25f, 0.25f, 0.0f,
					   0.75f, 0.25f, 0.0f,
					   0.75f, 0.75f, 0.0f,
					   0.25f, 0.75f, 0.0f)

    greenBatch.Begin(GL_TRIANGLE_FAN, 4)
    greenBatch.CopyVertexData3f(vBlock)
    greenBatch.End()


    val vBlock2 = Array( -0.75f, 0.25f, 0.0f,
						-0.25f, 0.25f, 0.0f,
						-0.25f, 0.75f, 0.0f,
						-0.75f, 0.75f, 0.0f)

    redBatch.Begin(GL_TRIANGLE_FAN, 4)
    redBatch.CopyVertexData3f(vBlock2)
    redBatch.End()


    val vBlock3 = Array( -0.75f, -0.75f, 0.0f,
						-0.25f, -0.75f, 0.0f,
						-0.25f, -0.25f, 0.0f,
						-0.75f, -0.25f, 0.0f)

    blueBatch.Begin(GL_TRIANGLE_FAN, 4)
    blueBatch.CopyVertexData3f(vBlock3)
    blueBatch.End()


    val vBlock4 = Array( 0.25f, -0.75f, 0.0f,
						0.75f, -0.75f, 0.0f,
						0.75f, -0.25f, 0.0f,
						0.25f, -0.25f, 0.0f)

    blackBatch.Begin(GL_TRIANGLE_FAN, 4)
    blackBatch.CopyVertexData3f(vBlock4)
    blackBatch.End()
  }

// Respond to arrow keys by moving the camera frame of reference
  def SpecialKeys(key : Int, x : Int, y : Int) {
	val stepSize = 0.025f

	var blockX = vVerts(0)   // Upper left X
	var blockY = vVerts(7)  // Upper left Y

	if(key == GLUT_KEY_UP)
	  blockY += stepSize

	if(key == GLUT_KEY_DOWN)
	  blockY -= stepSize

	if(key == GLUT_KEY_LEFT)
	  blockX -= stepSize

	if(key == GLUT_KEY_RIGHT)
	  blockX += stepSize

	// Collision detection
	if(blockX < -1.0f) blockX = -1.0f
	if(blockX > (1.0f - blockSize * 2)) blockX = 1.0f - blockSize * 2
	if(blockY < -1.0f + blockSize * 2)  blockY = -1.0f + blockSize * 2
	if(blockY > 1.0f) blockY = 1.0f

	// Recalculate vertex positions
	vVerts(0) = blockX
	vVerts(1) = blockY - blockSize*2

	vVerts(3) = blockX + blockSize*2
	vVerts(4) = blockY - blockSize*2

	vVerts(6) = blockX + blockSize*2
	vVerts(7) = blockY

	vVerts(9) = blockX
	vVerts(10) = blockY

	squareBatch.CopyVertexData3f(vVerts)

	glutPostRedisplay()
  }


///////////////////////////////////////////////////////////////////////////////
// Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT)

	val vRed = new Vector4f(1.0f, 0.0f, 0.0f, 0.5f )
    val vGreen = new Vector4f( 0.0f, 1.0f, 0.0f, 1.0f )
    val vBlue = new Vector4f( 0.0f, 0.0f, 1.0f, 1.0f )
    val vBlack = new Vector4f( 0.0f, 0.0f, 0.0f, 1.0f )


    shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vGreen)
    greenBatch.Draw()

    shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vRed)
    redBatch.Draw()

    shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vBlue)
    blueBatch.Draw()

    shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vBlack)
    blackBatch.Draw()


    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    shaderManager.UseStockShader(GLT_SHADER_IDENTITY, vRed)
    squareBatch.Draw()
    glDisable(GL_BLEND)


	// Flush drawing commands
	glutSwapBuffers()
  }



///////////////////////////////////////////////////////////////////////////////
// Window has changed size, or has just been created. In either case, we need
// to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
	glViewport(0, 0, w, h)
  }

///////////////////////////////////////////////////////////////////////////////
// Main entry point for GLUT based programs
  def main(argv : Array[String]) : Unit = {
	if (argv.size > 0)
	  gltSetWorkingDirectory(argv(0))

	glutInit(argv)
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH)
	glutInitWindowSize(800, 600)
	glutCreateWindow("Move Block with Arrow Keys to see blending")

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
