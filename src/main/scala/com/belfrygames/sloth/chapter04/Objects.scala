package com.belfrygames.sloth.chapter04

import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object Objects {
  /////////////////////////////////////////////////////////////////////////////////
  // An assortment of needed classes
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val cameraFrame = new GLFrame
  val objectFrame = new GLFrame
  val viewFrustum = new GLFrustum

  val sphereBatch = new GLTriangleBatch
  val torusBatch = new GLTriangleBatch
  val cylinderBatch = new GLTriangleBatch
  val coneBatch = new GLTriangleBatch
  val diskBatch = new GLTriangleBatch

  val transformPipeline = new GLGeometryTransform
  val shadowMatrix = new M3DMatrix44f

  val vGreen = M3DVector(0.0f, 1.0f, 0.0f, 1.0f)
  val vBlack = M3DVector(0.0f, 0.0f, 0.0f, 1.0f)

  // Keep track of effects step
  var nStep = 0;

  ///////////////////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering context.
  // This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
    // Black background
    glClearColor(0.7f, 0.7f, 0.7f, 1.0f );

	shaderManager.InitializeStockShaders();

	glEnable(GL_DEPTH_TEST);

	transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);

	cameraFrame.MoveForward(-15.0f);


    // Sphere
    gltMakeSphere(sphereBatch, 3.0f, 10, 20);

    // Torus
    gltMakeTorus(torusBatch, 3.0f, 0.75f, 15, 15);

    // Cylinder
    gltMakeCylinder(cylinderBatch, 2.0f, 2.0f, 3.0f, 13, 2);

    // Cone
    gltMakeCylinder(coneBatch, 2.0f, 0.0f, 3.0f, 13, 2);

    // Disk
    gltMakeDisk(diskBatch, 1.5f, 3.0f, 13, 3);
  }


  /////////////////////////////////////////////////////////////////////////
  def DrawWireFramedBatch(pBatch : GLTriangleBatch) {
    shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vGreen);
    pBatch.Draw();

    // Draw black outline
    glPolygonOffset(-1.0f, -1.0f);
    glEnable(GL_LINE_SMOOTH);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_POLYGON_OFFSET_LINE);
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    glLineWidth(2.5f);
    shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBlack);
    pBatch.Draw();

    // Restore polygon mode and depht testing
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    glDisable(GL_POLYGON_OFFSET_LINE);
    glLineWidth(1.0f);
    glDisable(GL_BLEND);
    glDisable(GL_LINE_SMOOTH);
  }

///////////////////////////////////////////////////////////////////////////////
// Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	modelViewMatrix.PushMatrix();
	val mCamera = new M3DMatrix44f
	cameraFrame.GetCameraMatrix(mCamera);
	modelViewMatrix.MultMatrix(mCamera);

	val mObjectFrame = new M3DMatrix44f
	objectFrame.GetMatrix(mObjectFrame);
	modelViewMatrix.MultMatrix(mObjectFrame);

	shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBlack);

	nStep match {
	  case 0 => DrawWireFramedBatch(sphereBatch);
	  case 1 => DrawWireFramedBatch(torusBatch);
	  case 2 => DrawWireFramedBatch(cylinderBatch);
	  case 3 => DrawWireFramedBatch(coneBatch);
	  case 4 => DrawWireFramedBatch(diskBatch);
	}

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
  // A normal ASCII key has been pressed.
  // In this case, advance the scene when the space bar is pressed
  def KeyPressFunc(key : Int, x : Int, y : Int) {
	if(key == 32) {
	  nStep += 1;

	  if(nStep > 4)
		nStep = 0;
	}

    nStep match {
	  case 0 => glutSetWindowTitle("Sphere");
	  case 1 => glutSetWindowTitle("Torus");
	  case 2 => glutSetWindowTitle("Cylinder");
	  case 3 => glutSetWindowTitle("Cone");
	  case 4 => glutSetWindowTitle("Disk");
	}

    glutPostRedisplay();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int)   {
	glViewport(0, 0, w, h);
	viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 500.0f);
	projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
	modelViewMatrix.LoadIdentity();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
 def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

	glutInit(args);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Sphere");
    glutReshapeFunc(ChangeSize);
    glutKeyboardFunc(KeyPressFunc);
    glutSpecialFunc(SpecialKeys);
    glutDisplayFunc(RenderScene);

//	GLenum err = glewInit();
//	if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	SetupRC();

	glutMainLoop();
	return 0;
  }
}
