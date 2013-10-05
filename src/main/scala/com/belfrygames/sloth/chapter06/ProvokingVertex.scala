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

object ProvokingVertex {
  import GLBatch._

  val triangleBatch = new GLBatch
  val shaderManager = GLShaderManager

  var myIdentityShader = 0

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
	glViewport(0, 0, w, h);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering context.
  // This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
	// Blue background
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f );

	shaderManager.InitializeStockShaders();

	// Load up a triangle
	val vVerts = Array(-0.5f, 0.0f, 0.0f,
					   0.5f, 0.0f, 0.0f,
					   0.0f, 0.5f, 0.0f)

	val vColors = Array(1.0f, 0.0f, 0.0f, 1.0f,
						0.0f, 1.0f, 0.0f, 1.0f,
						0.0f, 0.0f, 1.0f, 1.0f)

	triangleBatch.Begin(GL_TRIANGLES, 3);
	triangleBatch.CopyVertexData3f(vVerts);
	triangleBatch.CopyColorData4f(vColors);
	triangleBatch.End();

	myIdentityShader = gltLoadShaderPairWithAttributes("ProvokingVertex.vp", "ProvokingVertex.fp", 2,
													   GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_COLOR, "vColor");

	glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Cleanup
  def ShutdownRC() {
	glDeleteProgram(myIdentityShader);

  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	glUseProgram(myIdentityShader);
	triangleBatch.Draw();

	// Perform the buffer swap to display back buffer
	glutSwapBuffers();
  }

  var nToggle = 1;
  def KeyPressFunc(key : Int, x : Int, y : Int) {
	if(key == 32) {
	  nToggle += 1;

	  if(nToggle %2 == 0) {
		glProvokingVertex(GL_LAST_VERTEX_CONVENTION);
		glutSetWindowTitle("Provoking Vertex - Last Vertex - Press Space Bars");
	  } else {
		glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);
		glutSetWindowTitle("Provoking Vertex - First Vertex - Press Space Bars");
	  }

	  glutPostRedisplay();
	}
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Provoking Vertex - First Vertex - Press Space Bars");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);
	glutKeyboardFunc(KeyPressFunc);

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
