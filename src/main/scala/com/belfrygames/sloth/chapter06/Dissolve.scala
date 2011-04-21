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
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL32._

object Dissolve {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val torusBatch = new GLTriangleBatch
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  var ADSDissloveShader = 0	// The dissolving light shader
  var locAmbient = 0			// The location of the ambient color
  var locDiffuse = 0			// The location of the diffuse color
  var locSpecular = 0		// The location of the specular color
  var locLight = 0			// The location of the Light in eye coordinates
  var locMVP = 0				// The location of the ModelViewProjection matrix uniform
  var locMV = 0				// The location of the ModelView matrix uniform
  var locNM = 0				// The location of the Normal matrix uniform
  var locTexture = 0			// The location of the  texture uniform
  var locDissolveFactor = 0  // The location of the dissolve factor

  var cloudTexture = 0		// The cloud texture texture object

  // Load a TGA as a 2D Texture. Completely initialize the state
  def LoadTGATexture(szFileName : String, minFilter : Int, magFilter : Int, wrapMode : Int) {
		// Read the texture bits
		val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(szFileName)

		if(pBits == null)
			return false;

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		glTexImage2D(GL_TEXTURE_2D, 0, nComponents, nWidth, nHeight, 0,
								 eFormat, GL_UNSIGNED_BYTE, pBits);

    if(minFilter == GL_LINEAR_MIPMAP_LINEAR ||
       minFilter == GL_LINEAR_MIPMAP_NEAREST ||
       minFilter == GL_NEAREST_MIPMAP_LINEAR ||
       minFilter == GL_NEAREST_MIPMAP_NEAREST)
				 glGenerateMipmap(GL_TEXTURE_2D);

		return true;
  }


  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
		// Background
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f );

		glEnable(GL_DEPTH_TEST);

    shaderManager.InitializeStockShaders();
    viewFrame.MoveForward(4.0f);

    // Make the torus
    gltMakeTorus(torusBatch, .80f, 0.25f, 52, 26);

		ADSDissloveShader = gltLoadShaderPairWithAttributes("Dissolve.vp", "Dissolve.fp", 3, GLT_ATTRIBUTE_VERTEX, "vVertex",
																												GLT_ATTRIBUTE_NORMAL, "vNormal", GLT_ATTRIBUTE_TEXTURE0, "vTexCoords0");

		locAmbient = glGetUniformLocation(ADSDissloveShader, "ambientColor");
		locDiffuse = glGetUniformLocation(ADSDissloveShader, "diffuseColor");
		locSpecular = glGetUniformLocation(ADSDissloveShader, "specularColor");
		locLight = glGetUniformLocation(ADSDissloveShader, "vLightPosition");
		locMVP = glGetUniformLocation(ADSDissloveShader, "mvpMatrix");
		locMV  = glGetUniformLocation(ADSDissloveShader, "mvMatrix");
		locNM  = glGetUniformLocation(ADSDissloveShader, "normalMatrix");
		locTexture = glGetUniformLocation(ADSDissloveShader, "cloudTexture");
		locDissolveFactor = glGetUniformLocation(ADSDissloveShader, "dissolveFactor");

		cloudTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_1D, cloudTexture);
		LoadTGATexture("Clouds.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);
  }

  // Cleanup
  def ShutdownRC() {

  }

  // After googling and cleaning this is fmod I guess
  def fmod(x : Float, y : Float) = {
		def modf(x : Float) = x - x.toInt

		if (y == 0) 0 else {modf(x / y) * y}
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


		glUseProgram(ADSDissloveShader);
		glUniform4(locAmbient, vAmbientColor);
		glUniform4(locDiffuse, vDiffuseColor);
		glUniform4(locSpecular, vSpecularColor);
		glUniform3(locLight, vEyeLight);
		glUniformMatrix4(locMVP, false, transformPipeline.GetModelViewProjectionMatrix());
		glUniformMatrix4(locMV, false, transformPipeline.GetModelViewMatrix());
		glUniformMatrix3(locNM, false, transformPipeline.GetNormalMatrix());
		glUniform1i(locTexture, 1);

		val fFactor = fmod(rotTimer.GetElapsedSeconds(), 10.0f) / 10.0f;
		glUniform1f(locDissolveFactor, fFactor);
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
		glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
		glutInitWindowSize(800, 600);
		glutCreateWindow("Help me, I'm melting!");
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
		return 0;
  }
}
