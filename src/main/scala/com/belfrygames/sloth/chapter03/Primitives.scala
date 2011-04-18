package com.belfrygames.sloth.chapter03

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

import scala.math._

object Primitives {
  /////////////////////////////////////////////////////////////////////////////////
  // An assortment of needed classes
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val cameraFrame = new GLFrame;
  val objectFrame = new GLFrame;
  val viewFrustum = new GLFrustum;

  val pointBatch = new GLBatch
  val lineBatch = new GLBatch
  val lineStripBatch = new GLBatch
  val lineLoopBatch = new GLBatch
  val triangleBatch = new GLBatch
  val triangleStripBatch = new GLBatch
  val triangleFanBatch = new GLBatch

  val torusBatch = new GLTriangleBatch;
  val transformPipeline = new GLGeometryTransform
  val shadowMatrix = new M3DMatrix44f

  val vGreen = M3DVector(0.0f, 1.0f, 0.0f, 1.0f);
  val vBlack = M3DVector(0.0f, 0.0f, 0.0f, 1.0f);


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

    //////////////////////////////////////////////////////////////////////
    // Some points, more or less in the shape of Florida
    val vCoast = Array[Float](2.80f, 1.20f, 0.0f , 2.0f,  1.20f, 0.0f ,
							  2.0f,  1.08f, 0.0f ,  2.0f,  1.08f, 0.0f ,
							  0.0f,  0.80f, 0.0f ,  -.32f, 0.40f, 0.0f ,
							  -.48f, 0.2f, 0.0f ,   -.40f, 0.0f, 0.0f ,
							  -.60f, -.40f, 0.0f ,  -.80f, -.80f, 0.0f ,
							  -.80f, -1.4f, 0.0f ,  -.40f, -1.60f, 0.0f ,
							  0.0f, -1.20f, 0.0f ,   .2f, -.80f, 0.0f ,
							  .48f, -.40f, 0.0f ,   .52f, -.20f, 0.0f ,
							  .48f,  .20f, 0.0f ,   .80f,  .40f, 0.0f ,
							  1.20f, .80f, 0.0f ,   1.60f, .60f, 0.0f ,
							  2.0f, .60f, 0.0f ,    2.2f, .80f, 0.0f ,
							  2.40f, 1.0f, 0.0f ,   2.80f, 1.0f, 0.0f );

    // Load point batch
    pointBatch.Begin(GL_POINTS, 24);
    pointBatch.CopyVertexData3f(vCoast);
    pointBatch.End();

    // Load as a bunch of line segments
    lineBatch.Begin(GL_LINES, 24);
    lineBatch.CopyVertexData3f(vCoast);
    lineBatch.End();

    // Load as a single line segment
    lineStripBatch.Begin(GL_LINE_STRIP, 24);
    lineStripBatch.CopyVertexData3f(vCoast);
    lineStripBatch.End();

    // Single line, connect first and last points
    lineLoopBatch.Begin(GL_LINE_LOOP, 24);
    lineLoopBatch.CopyVertexData3f(vCoast);
    lineLoopBatch.End();

    // For Triangles, we'll make a Pyramid
    val vPyramid = Array[Float](-2.0f, 0.0f, -2.0f,
								2.0f, 0.0f, -2.0f,
								0.0f, 4.0f, 0.0f,

								2.0f, 0.0f, -2.0f,
								2.0f, 0.0f, 2.0f,
								0.0f, 4.0f, 0.0f,

								2.0f, 0.0f, 2.0f,
								-2.0f, 0.0f, 2.0f,
								0.0f, 4.0f, 0.0f,

								-2.0f, 0.0f, 2.0f,
								-2.0f, 0.0f, -2.0f,
								0.0f, 4.0f, 0.0f)

    triangleBatch.Begin(GL_TRIANGLES, 12);
    triangleBatch.CopyVertexData3f(vPyramid);
    triangleBatch.End();


    // For a Triangle fan, just a 6 sided hex. Raise the center up a bit
    val vPoints = new M3DVector3fArray(100);    // Scratch array, more than we need
    var nVerts = 0;
    val r = 3.0f;
    vPoints(nVerts)(0) = 0.0f;
    vPoints(nVerts)(1) = 0.0f;
    vPoints(nVerts)(2) = 0.0f;

	var angle = 0.0f
    while(angle < M3D_2PI) {
	  nVerts += 1;
	  vPoints(nVerts)(0) = cos(angle).toFloat * r;
	  vPoints(nVerts)(1) = sin(angle).toFloat * r;
	  vPoints(nVerts)(2) = -0.5f;

	  angle += M3D_2PI.toFloat / 6.0f
	}

    // Close the fan
    nVerts += 1;
    vPoints(nVerts)(0) = r;
    vPoints(nVerts)(1) = 0;
    vPoints(nVerts)(2) = 0.0f;

    // Load it up
    triangleFanBatch.Begin(GL_TRIANGLE_FAN, 8);
    triangleFanBatch.CopyVertexData3f(vPoints);
    triangleFanBatch.End();

    // For triangle strips, a little ring or cylinder segment
    var iCounter = 0;
    val radius = 3.0f;
	angle = 0.0f
    while(angle <= (2.0f*M3D_PI))
	{
	  val x = radius * sin(angle).toFloat;
	  val y = radius * cos(angle).toFloat;

	  // Specify the point and move the Z value up a little
	  vPoints(iCounter)(0) = x;
	  vPoints(iCounter)(1) = y;
	  vPoints(iCounter)(2) = -0.5f;
	  iCounter += 1;

	  vPoints(iCounter)(0) = x;
	  vPoints(iCounter)(1) = y;
	  vPoints(iCounter)(2) = 0.5f;
	  iCounter += 1;
	  angle += 0.3f
	}

    // Close up the loop
    vPoints(iCounter)(0) = vPoints(0)(0);
    vPoints(iCounter)(1) = vPoints(0)(1);
    vPoints(iCounter)(2) = -0.5f;
    iCounter += 1;

    vPoints(iCounter)(0) = vPoints(1)(0);
    vPoints(iCounter)(1) = vPoints(1)(1);
    vPoints(iCounter)(2) = 0.5f;
    iCounter += 1;

    // Load the triangle strip
    triangleStripBatch.Begin(GL_TRIANGLE_STRIP, iCounter);
    triangleStripBatch.CopyVertexData3f(vPoints);
    triangleStripBatch.End();
  }


  /////////////////////////////////////////////////////////////////////////
  def DrawWireFramedBatch(pBatch : GLBatch) {
    // Draw the batch solid green
    shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vGreen);
    pBatch.Draw();

    // Draw black outline
    glPolygonOffset(-1.0f, -1.0f);      // Shift depth values
    glEnable(GL_POLYGON_OFFSET_LINE);

    // Draw lines antialiased
    glEnable(GL_LINE_SMOOTH);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    // Draw black wireframe version of geometry
    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    glLineWidth(2.5f);
    shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBlack);
    pBatch.Draw();

    // Put everything back the way we found it
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    glDisable(GL_POLYGON_OFFSET_LINE);
    glLineWidth(1.0f);
    glDisable(GL_BLEND);
    glDisable(GL_LINE_SMOOTH);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  val mCamera = new M3DMatrix44f
  val mObjectFrame = new M3DMatrix44f
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	modelViewMatrix.PushMatrix();
	cameraFrame.GetCameraMatrix(mCamera);
	modelViewMatrix.MultMatrix(mCamera);

	objectFrame.GetMatrix(mObjectFrame);
	modelViewMatrix.MultMatrix(mObjectFrame);

	shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBlack);

	nStep match {
	  case 0 => {
		  glPointSize(4.0f);
		  pointBatch.Draw();
		  glPointSize(1.0f);
		}
	  case 1 => {
		  glLineWidth(2.0f);
		  lineBatch.Draw();
		  glLineWidth(1.0f);
		}
	  case 2 => {
		  glLineWidth(2.0f);
		  lineStripBatch.Draw();
		  glLineWidth(1.0f);
		}
	  case 3 => {
		  glLineWidth(2.0f);
		  lineLoopBatch.Draw();
		  glLineWidth(1.0f);
		}
	  case 4 => {
		  DrawWireFramedBatch(triangleBatch);
		}
	  case 5 => {
		  DrawWireFramedBatch(triangleStripBatch);
		}
	  case 6 => {
		  DrawWireFramedBatch(triangleFanBatch);
		}
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
	if(key == 32)
	{
	  nStep += 1;

	  if(nStep > 6)
		nStep = 0;
	}

    nStep match {
	  case 0 => glutSetWindowTitle("GL_POINTS");
	  case 1 => glutSetWindowTitle("GL_LINES");
	  case 2 => glutSetWindowTitle("GL_LINE_STRIP");
	  case 3 => glutSetWindowTitle("GL_LINE_LOOP");
	  case 4 => glutSetWindowTitle("GL_TRIANGLES");
	  case 5 => glutSetWindowTitle("GL_TRIANGLE_STRIP");
	  case 6 => glutSetWindowTitle("GL_TRIANGLE_FAN");
	}

    glutPostRedisplay();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
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
	glutCreateWindow("GL_POINTS");
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
