package com.belfrygames.sloth.chapter06

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.Math3D.M3DVector._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import java.nio.ByteBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL32._

object ToonShader {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val torusBatch = new GLTriangleBatch
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  var toonShader = 0	        // The dissolving light shader
  var locLight = 0			// The location of the Light in eye coordinates
  var locMVP = 0				// The location of the ModelViewProjection matrix uniform
  var locMV = 0				// The location of the ModelView matrix uniform
  var locNM = 0				// The location of the Normal matrix uniform
  var locColorTable = 0		// The location of the color table

  var texture = 0

  // Should move to happier place
  implicit def getByteBuffer(a : Array[Byte]) : ByteBuffer = {
	val buffer = BufferUtils.createByteBuffer(a.length)
	buffer.put(a)
	buffer.flip()
	buffer
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
	// Background
	glClearColor(0.025f, 0.25f, 0.25f, 1.0f );

	glEnable(GL_DEPTH_TEST);

    shaderManager.InitializeStockShaders();
    viewFrame.MoveForward(4.0f);

    // Make the torus
    gltMakeTorus(torusBatch, .80f, 0.25f, 52, 26);

	toonShader = gltLoadShaderPairWithAttributes("ToonShader.vp", "ToonShader.fp", 2, GLT_ATTRIBUTE_VERTEX, "vVertex",
												 GLT_ATTRIBUTE_NORMAL, "vNormal");

	locLight = glGetUniformLocation(toonShader, "vLightPosition");
	locMVP = glGetUniformLocation(toonShader, "mvpMatrix");
	locMV  = glGetUniformLocation(toonShader, "mvMatrix");
	locNM  = glGetUniformLocation(toonShader, "normalMatrix");
	locColorTable = glGetUniformLocation(toonShader, "colorTable");

	texture = glGenTextures();
	glBindTexture(GL_TEXTURE_1D, texture);
	val textureData = Array[Byte](32,  0, 0,
								  64,  0, 0,
								  128.toByte, 0, 0,
								  255.toByte, 0, 0);

	glTexImage1D(GL_TEXTURE_1D, 0, GL_RGB, 4, 0, GL_RGB, GL_UNSIGNED_BYTE, textureData);
	glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  }

  // Cleanup
  def ShutdownRC() {
	glDeleteTextures(texture);
  }


// Called to draw scene
  lazy val rotTimer = new CStopWatch
  val vEyeLight = M3DVector( -100.0f, 100.0f, 100.0f )
  val vAmbientColor = M3DVector( 0.1f, 0.1f, 0.1f, 1.0f )
  val vDiffuseColor = M3DVector( 0.1f, 1.0f, 0.1f, 1.0f )
  val vSpecularColor = M3DVector( 1.0f, 1.0f, 1.0f, 1.0f )
  def RenderScene() {

	// Clear the window and the depth buffer
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    modelViewMatrix.PushMatrix(viewFrame);
	modelViewMatrix.Rotate(rotTimer.GetElapsedSeconds() * 10.0f, 0.0f, 1.0f, 0.0f);


	glUseProgram(toonShader);
	glUniform3(locLight, vEyeLight);
	glUniformMatrix4(locMVP, false, transformPipeline.GetModelViewProjectionMatrix());
	glUniformMatrix4(locMV, false, transformPipeline.GetModelViewMatrix());
	glUniformMatrix3(locNM, false, transformPipeline.GetNormalMatrix());
	glUniform1i(locColorTable, 0);
    torusBatch.Draw();

    modelViewMatrix.PopMatrix();


    glutSwapBuffers();
	glutPostRedisplay();
  }


  def ChangeSize(w: Int, _h : Int) {
	// Prevent a divide by zero
	val h = if (_h == 0) 1 else _h

	// Set Viewport to window dimensions
	glViewport(0, 0, w, h);

	viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 100.0f);

    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

	glutInit(args);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Cell (toon) shading");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

//	GLenum err = glewInit();
//	if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//    }

	SetupRC();
	glutMainLoop();
	ShutdownRC();
  }
}
