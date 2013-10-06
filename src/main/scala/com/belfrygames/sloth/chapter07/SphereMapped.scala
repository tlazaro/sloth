package com.belfrygames.sloth.chapter07

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.IntArray._
import com.belfrygames.sloth.Math3D.M3DVector._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL31._
import org.lwjgl.opengl.GL33._

object SphereMapped {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val torusBatch = new GLTriangleBatch
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  var sphereMapShader = 0
  // The dissolving light shader
  var locMVP = 0
  // The location of the ModelViewProjection matrix uniform
  var locMV = 0
  // The location of the Modelview matrix
  var locNormalMatrix = 0
  // The location of the Normal Matrix
  var locTexture = 0 // The location of the  texture uniform

  var sphereTexture = 0 // The cloud texture texture object

  // Load a TGA as a 2D Texture. Completely initialize the state
  def LoadTGATexture(szFileName: String, minFilter: Int, magFilter: Int, wrapMode: Int): Boolean = {
    // Read the texture bits
    val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(szFileName)
    if (pBits == null)
      return false;

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glTexImage2D(GL_TEXTURE_2D, 0, nComponents, nWidth, nHeight, 0, eFormat, GL_UNSIGNED_BYTE, pBits);

    if (minFilter == GL_LINEAR_MIPMAP_LINEAR ||
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
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    glEnable(GL_DEPTH_TEST);

    shaderManager.InitializeStockShaders();
    viewFrame.MoveForward(4.0f);

    // Make the torus
    gltMakeTorus(torusBatch, .80f, 0.25f, 52, 26);

    sphereMapShader = gltLoadShaderPairWithAttributes("SphereMapped.vp", "SphereMapped.fp", 3, GLT_ATTRIBUTE_VERTEX, "vVertex",
      GLT_ATTRIBUTE_NORMAL, "vNormal", GLT_ATTRIBUTE_TEXTURE0, "vTexCoords0");

    locMVP = glGetUniformLocation(sphereMapShader, "mvpMatrix");
    locTexture = glGetUniformLocation(sphereMapShader, "sphereMap");
    locMV = glGetUniformLocation(sphereMapShader, "mvMatrix");
    locNormalMatrix = glGetUniformLocation(sphereMapShader, "normalMatrix");

    sphereTexture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, sphereTexture);
    LoadTGATexture("SphereMap.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);
  }

  // Cleanup
  def ShutdownRC() {
    glDeleteTextures(sphereTexture);
  }


  // Called to draw scene
  lazy val rotTimer = new CStopWatch

  def RenderScene() {

    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    modelViewMatrix.PushMatrix(viewFrame);
    modelViewMatrix.Rotate(rotTimer.GetElapsedSeconds() * 10.0f, 0.0f, 1.0f, 0.0f);

    glUseProgram(sphereMapShader);
    glUniformMatrix4(locMVP, false, transformPipeline.GetModelViewProjectionMatrix());
    glUniformMatrix4(locMV, false, transformPipeline.GetModelViewMatrix());
    glUniformMatrix3(locNormalMatrix, false, transformPipeline.GetNormalMatrix(false));
    glUniform1i(locTexture, 0);

    torusBatch.Draw();

    modelViewMatrix.PopMatrix();

    glutSwapBuffers();
    glutPostRedisplay();
  }

  def ChangeSize(w: Int, _h: Int) {
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
    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Sphere Mapping");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

    //		GLenum err = glewInit();
    //		if (GLEW_OK != err) {
    //			fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //			return 1;
    //    }

    SetupRC();
    glutMainLoop();
    ShutdownRC();
  }
}
