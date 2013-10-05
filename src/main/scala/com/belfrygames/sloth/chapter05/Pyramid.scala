package com.belfrygames.sloth.chapter05

import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL30._

object Pyramid {
  // Pyramid.cpp
  // OpenGL SuperBible, Chapter 5
  // Demonstrates Texture mapping a pyramid
  // Program by Richard S. Wright Jr.

  /////////////////////////////////////////////////////////////////////////////////
  // An assortment of needed classes
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val cameraFrame = new GLFrame
  val objectFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val transformPipeline = new GLGeometryTransform

  val pyramidBatch = new GLBatch

  var textureID : Int = _

  val shadowMatrix = new M3DMatrix44f


  def MakePyramid(pyramidBatch : GLBatch) {
	pyramidBatch.Begin(GL_TRIANGLES, 18, 1);

	// Bottom of pyramid
	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	pyramidBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	pyramidBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	pyramidBatch.Vertex3f(-1.0f, -1.0f, 1.0f);

	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	pyramidBatch.Normal3f(0.0f, -1.0f, 0.0f);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	pyramidBatch.Vertex3f(1.0f, -1.0f, 1.0f);


	val vApex = M3DVector( 0.0f, 1.0f, 0.0f );
	val vFrontLeft = M3DVector( -1.0f, -1.0f, 1.0f );
	val vFrontRight = M3DVector( 1.0f, -1.0f, 1.0f );
	val vBackLeft = M3DVector( -1.0f, -1.0f, -1.0f );
	val vBackRight = M3DVector( 1.0f, -1.0f, -1.0f );
	val n = new M3DVector3f

	// Front of Pyramid
	m3dFindNormal(n, vApex, vFrontLeft, vFrontRight);
	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.5f, 1.0f);
	pyramidBatch.Vertex3fv(vApex);		// Apex

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3fv(vFrontLeft);		// Front left corner

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	pyramidBatch.Vertex3fv(vFrontRight);		// Front right corner


	m3dFindNormal(n, vApex, vBackLeft, vFrontLeft);
	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.5f, 1.0f);
	pyramidBatch.Vertex3fv(vApex);		// Apex

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	pyramidBatch.Vertex3fv(vBackLeft);		// Back left corner

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3fv(vFrontLeft);		// Front left corner

	m3dFindNormal(n, vApex, vFrontRight, vBackRight);
	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.5f, 1.0f);
	pyramidBatch.Vertex3fv(vApex);				// Apex

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	pyramidBatch.Vertex3fv(vFrontRight);		// Front right corner

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3fv(vBackRight);			// Back right cornder


	m3dFindNormal(n, vApex, vBackRight, vBackLeft);
	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.5f, 1.0f);
	pyramidBatch.Vertex3fv(vApex);		// Apex

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	pyramidBatch.Vertex3fv(vBackRight);		// Back right cornder

	pyramidBatch.Normal3fv(n);
	pyramidBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	pyramidBatch.Vertex3fv(vBackLeft);		// Back left corner

	pyramidBatch.End();
  }

  // Load a TGA as a 2D Texture. Completely initialize the state
  def LoadTGATexture(szFileName : String, minFilter : Int, magFilter : Int, wrapMode : Int) : Boolean = {
	// Read the texture bits
	val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(szFileName)

	if(pBits == null)
	  return false;

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	glTexImage2D(GL_TEXTURE_2D, 0, nComponents, nWidth, nHeight, 0, eFormat, GL_UNSIGNED_BYTE, pBits);

    if(minFilter == GL_LINEAR_MIPMAP_LINEAR ||
       minFilter == GL_LINEAR_MIPMAP_NEAREST ||
       minFilter == GL_NEAREST_MIPMAP_LINEAR ||
       minFilter == GL_NEAREST_MIPMAP_NEAREST)
		 glGenerateMipmap(GL_TEXTURE_2D);

	return true;
  }


  ///////////////////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering context.
  // This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
    // Black background
    glClearColor(0.7f, 0.7f, 0.7f, 1.0f );

	shaderManager.InitializeStockShaders();

	glEnable(GL_DEPTH_TEST);

    textureID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, textureID);
    LoadTGATexture("stone.tga", GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);

    MakePyramid(pyramidBatch);

	cameraFrame.MoveForward(-7.0f);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Cleanup... such as deleting texture objects
  def ShutdownRC() {
    glDeleteTextures(textureID);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  val vLightPos  = M3DVector( 1.0f, 1.0f, 0.0f, 1.0f );
  val vWhite = M3DVector( 1.0f, 1.0f, 1.0f, 1.0f );
  val mCamera = new M3DMatrix44f
  val mObjectFrame = new M3DMatrix44f;
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	modelViewMatrix.PushMatrix();
	cameraFrame.GetCameraMatrix(mCamera);
	modelViewMatrix.MultMatrix(mCamera);

	objectFrame.GetMatrix(mObjectFrame);
	modelViewMatrix.MultMatrix(mObjectFrame);

	glBindTexture(GL_TEXTURE_2D, textureID);
	shaderManager.UseStockShader(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF,
								 transformPipeline.GetModelViewMatrix(),
								 transformPipeline.GetProjectionMatrix(),
								 vLightPos, vWhite, 0);

	pyramidBatch.Draw();


	modelViewMatrix.PopMatrix();

	// Flush drawing commands
	glutSwapBuffers();
  }


  // Respond to arrow keys by moving the camera frame of reference
  def SpecialKeys(key : Int, x : Int, y : Int) {
	if(key == GLUT_KEY_UP)
	  objectFrame.RotateWorld(m3dDegToRad(-5.0f), 1.0f, 0.0f, 0.0f);

	if(key == GLUT_KEY_DOWN)
	  objectFrame.RotateWorld(m3dDegToRad(5.0f), 1.0f, 0.0f, 0.0f);

	if(key == GLUT_KEY_LEFT)
	  objectFrame.RotateWorld(m3dDegToRad(-5.0f), 0.0f, 1.0f, 0.0f);

	if(key == GLUT_KEY_RIGHT)
	  objectFrame.RotateWorld(m3dDegToRad(5.0f), 0.0f, 1.0f, 0.0f);

	glutPostRedisplay();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
	glViewport(0, 0, w, h);
	viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 500.0f);
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
	glutCreateWindow("Pyramid");
    glutReshapeFunc(ChangeSize);
    glutSpecialFunc(SpecialKeys);
    glutDisplayFunc(RenderScene);

//	GLenum err = glewInit();
//	if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	SetupRC();

	glutMainLoop();

    ShutdownRC();
  }
}
