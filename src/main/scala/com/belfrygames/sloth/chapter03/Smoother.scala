package com.belfrygames.sloth.chapter03

import com.belfrygames.sloth.Math3D.M3DVector
import com.belfrygames.sloth.Math3D.M3DVector3fArray
import com.belfrygames.sloth._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

import scala.math._

object Smoother {
  // Smoother.cpp
  // OpenGL SuperBible
  // Demonstrates point and line antialiasing
  // Program by Richard S. Wright Jr.

  val shaderManager = GLShaderManager
  val viewFrustum = new GLFrustum
  val smallStarBatch = new GLBatch
  val mediumStarBatch = new GLBatch
  val largeStarBatch = new GLBatch
  val mountainRangeBatch = new GLBatch
  val moonBatch = new GLBatch

// Array of small stars
  val SMALL_STARS    = 100
  val MEDIUM_STARS   =  40
  val LARGE_STARS    =  15

  val SCREEN_X       = 800
  val SCREEN_Y       = 600

///////////////////////////////////////////////////////////////////////
// Reset flags as appropriate in response to menu selections
  def ProcessMenu(value : Int) {
    value match {
			case 1 => {
					// Turn on antialiasing, and give hint to do the best
					// job possible.
					glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
					glEnable(GL_BLEND);
					glEnable(GL_POINT_SMOOTH);
					glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
					glEnable(GL_LINE_SMOOTH);
					glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
					glEnable(GL_POLYGON_SMOOTH);
					glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);
				}
			case 2 => {
					// Turn off blending and all smoothing
					glDisable(GL_BLEND);
					glDisable(GL_LINE_SMOOTH);
					glDisable(GL_POINT_SMOOTH);
				}
			case _ =>
		}

    // Trigger a redraw
    glutPostRedisplay();
  }


///////////////////////////////////////////////////
// Called to draw scene
  def RenderScene() {
    // Clear the window
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Everything is white
    val vWhite = M3DVector(1.0f, 1.0f, 1.0f, 1.0f);
    shaderManager.UseStockShader(GLT_SHADER_FLAT, viewFrustum.GetProjectionMatrix(), vWhite);

    // Draw small stars
    glPointSize(1.0f);
    smallStarBatch.Draw();

    // Draw medium sized stars
    glPointSize(4.0f);
    mediumStarBatch.Draw();

    // Draw largest stars
    glPointSize(8.0f);
    largeStarBatch.Draw();

    // Draw the "moon"
    moonBatch.Draw();

    // Draw distant horizon
    glLineWidth(3.5f);
    mountainRangeBatch.Draw();

    moonBatch.Draw();

    // Swap buffers
    glutSwapBuffers();
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
		val vVerts = new M3DVector3fArray(SMALL_STARS)

    shaderManager.InitializeStockShaders();

    // Populate star list
		val rand = new scala.util.Random
	
    smallStarBatch.Begin(GL_POINTS, SMALL_STARS);
    for(i <- 0 until SMALL_STARS) {
			vVerts(i)(0) = (rand.nextFloat * SCREEN_X);
			vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
			vVerts(i)(2) = 0.0f;
		}

    smallStarBatch.CopyVertexData3f(vVerts);
    smallStarBatch.End();

    // Populate star list
    mediumStarBatch.Begin(GL_POINTS, MEDIUM_STARS);
    for(i <- 0 until MEDIUM_STARS) {
			vVerts(i)(0) = (rand.nextFloat * SCREEN_X);
			vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
			vVerts(i)(2) = 0.0f;
		}
    mediumStarBatch.CopyVertexData3f(vVerts);
    mediumStarBatch.End();

    // Populate star list
    largeStarBatch.Begin(GL_POINTS, LARGE_STARS);
    for(i <- 0 until LARGE_STARS) {
			vVerts(i)(0) = (rand.nextFloat * SCREEN_X);
			vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
			vVerts(i)(2) = 0.0f;
		}
    largeStarBatch.CopyVertexData3f(vVerts);
    largeStarBatch.End();

    val vMountains = Array[Float]( 0.0f, 25.0f, 0.0f,
																	50.0f, 100.0f, 0.0f,
																	100.0f, 25.0f, 0.0f,
																	225.0f, 125.0f, 0.0f,
																	300.0f, 50.0f, 0.0f,
																	375.0f, 100.0f, 0.0f,
																	460.0f, 25.0f, 0.0f,
																	525.0f, 100.0f, 0.0f,
																	600.0f, 20.0f, 0.0f,
																	675.0f, 70.0f, 0.0f,
																	750.0f, 25.0f, 0.0f,
																	800.0f, 90.0f, 0.0f)

    mountainRangeBatch.Begin(GL_LINE_STRIP, 12);
    mountainRangeBatch.CopyVertexData3f(vMountains);
    mountainRangeBatch.End();

    // The Moon
    val x = 700.0f;     // Location and radius of moon
    val y = 500.0f;
    val r = 50.0f;
    var angle = 0.0f;   // Another looping variable

    moonBatch.Begin(GL_TRIANGLE_FAN, 34);
    var nVerts = 0;
    vVerts(nVerts)(0) = x;
    vVerts(nVerts)(1) = y;
    vVerts(nVerts)(2) = 0.0f;

		while(angle < 2.0f * 3.141592f) {
			nVerts += 1;
			vVerts(nVerts)(0) = x + cos(angle).toFloat * r;
			vVerts(nVerts)(1) = y + sin(angle).toFloat * r;
			vVerts(nVerts)(2) = 0.0f;

			angle += 0.2f
		}
    nVerts += 1;

    vVerts(nVerts)(0) = x + r;
    vVerts(nVerts)(1) = y;
    vVerts(nVerts)(2) = 0.0f;
    moonBatch.CopyVertexData3f(vVerts);
    moonBatch.End();

    // Black background
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
  }



  def ChangeSize(w : Int, h : Int)
  {
    // Prevent a divide by zero
    // Set Viewport to window dimensions
    glViewport(0, 0, w, if(h == 0) 1 else h);

    // Establish clipping volume (left, right, bottom, top, near, far)
    viewFrustum.SetOrthographic(0.0f, SCREEN_X, 0.0f, SCREEN_Y, -1.0f, 1.0f);
  }

  def main(argv : Array[String]) : Unit = {
		glutInit(argv);
		glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
		glutInitWindowSize(800, 600);
		glutCreateWindow("Smoothing Out The Jaggies");

		// Create the Menu
//	glutCreateMenu(ProcessMenu);
//	glutAddMenuEntry("Antialiased Rendering",1);
//	glutAddMenuEntry("Normal Rendering",2);
//	glutAttachMenu(GLUT_RIGHT_BUTTON);

		// TODO Remove Temporary avoidance of menu
		glutSpecialFunc((key : Int, x : Int, y :Int) => key match {
				case GLUT_KEY_F1 => ProcessMenu(1)
				case GLUT_KEY_F2 => ProcessMenu(2)
			})

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
