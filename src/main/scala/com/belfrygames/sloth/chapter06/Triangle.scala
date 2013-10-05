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

object Triangle {
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
	glClearColor(0.0f, 0.0f, 1.0f, 1.0f );

	shaderManager.InitializeStockShaders();

	// Load up a triangle
	val vVerts = Array(-0.5f, 0.0f, 0.0f,
					   0.5f, 0.0f, 0.0f,
					   0.0f, 0.5f, 0.0f)

	triangleBatch.Begin(GL_TRIANGLES, 3);
	triangleBatch.CopyVertexData3f(vVerts);
	triangleBatch.End();

	myIdentityShader = shaderManager.LoadShaderPairWithAttributes("Identity.vp", "Identity.fp", 1, GLT_ATTRIBUTE_VERTEX, "vVertex");
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Cleanup
  def ShutdownRC() {
	glDeleteProgram(myIdentityShader);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  val vRed = M3DVector(1.0f, 0.0f, 0.0f, 1.0f);
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	glUseProgram(myIdentityShader);

	val iColorLocation = glGetUniformLocation(myIdentityShader, "vColor");
	glUniform4(iColorLocation, vRed);

	triangleBatch.Draw();

	// Perform the buffer swap to display back buffer
	glutSwapBuffers();
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Triangle");
    glutReshapeFunc(ChangeSize);
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
