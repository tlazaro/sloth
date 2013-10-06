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

object NormalMapped {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val sphereBatch = new GLTriangleBatch
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  var normalMapShader = 0
  // The textured diffuse light shader
  var locAmbient = 0
  // The location of the ambient color
  var locDiffuse = 0
  // The location of the diffuse color
  var locLight = 0
  // The location of the Light in eye coordinates
  var locMVP = 0
  // The location of the ModelViewProjection matrix uniform
  var locMV = 0
  // The location of the ModelView matrix uniform
  var locNM = 0
  // The location of the Normal matrix uniform
  var locColorMap = 0
  // The location of the color map sampler
  var locNormalMap = 0
  // The location of the normal map sampler
  val texture = new IntArray(2) // Two textures, color map and normal map

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
    glEnable(GL_CULL_FACE);

    shaderManager.InitializeStockShaders();
    viewFrame.MoveForward(4.0f);

    // Make the sphere
    gltMakeSphere(sphereBatch, 1.0f, 52, 26);

    normalMapShader = gltLoadShaderPairWithAttributes("NormalMapped.vp", "NormalMapped.fp", 3, GLT_ATTRIBUTE_VERTEX, "vVertex",
      GLT_ATTRIBUTE_NORMAL, "vNormal", GLT_ATTRIBUTE_TEXTURE0, "vTexture0");

    locAmbient = glGetUniformLocation(normalMapShader, "ambientColor");
    locDiffuse = glGetUniformLocation(normalMapShader, "diffuseColor");
    locLight = glGetUniformLocation(normalMapShader, "vLightPosition");
    locMVP = glGetUniformLocation(normalMapShader, "mvpMatrix");
    locMV = glGetUniformLocation(normalMapShader, "mvMatrix");
    locNM = glGetUniformLocation(normalMapShader, "normalMatrix");
    locColorMap = glGetUniformLocation(normalMapShader, "colorMap");
    locNormalMap = glGetUniformLocation(normalMapShader, "normalMap");

    glGenTextures(texture);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture(0));
    LoadTGATexture("IceMoon.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, texture(1));
    LoadTGATexture("IceMoonBump.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_REPEAT);
  }

  // Cleanup
  def ShutdownRC() {
    glDeleteTextures(texture);
  }

  // Called to draw scene
  lazy val rotTimer = new CStopWatch
  val vEyeLight = M3DVector(-100.0f, 100.0f, 150.0f)
  val vAmbientColor = M3DVector(0.2f, 0.2f, 0.2f, 1.0f)
  val vDiffuseColor = M3DVector(1.0f, 1.0f, 1.0f, 1.0f)

  def RenderScene() {
    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    modelViewMatrix.PushMatrix(viewFrame);
    modelViewMatrix.Rotate(-90.0f, 1.0f, 0.0f, 0.0f);
    modelViewMatrix.Rotate(rotTimer.GetElapsedSeconds() * 10.0f, 0.0f, 0.0f, 1.0f);


    glUseProgram(normalMapShader);
    glUniform4(locAmbient, vAmbientColor);
    glUniform4(locDiffuse, vDiffuseColor);
    glUniform3(locLight, vEyeLight);
    glUniformMatrix4(locMVP, false, transformPipeline.GetModelViewProjectionMatrix());
    glUniformMatrix4(locMV, false, transformPipeline.GetModelViewMatrix());
    glUniformMatrix3(locNM, false, transformPipeline.GetNormalMatrix());
    glUniform1i(locColorMap, 0);
    glUniform1i(locNormalMap, 1);
    sphereBatch.Draw();

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
    glutCreateWindow("Lit Texture");
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
