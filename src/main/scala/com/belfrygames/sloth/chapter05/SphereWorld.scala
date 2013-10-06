package com.belfrygames.sloth.chapter05

import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth._
import com.belfrygames.sloth.IntArray._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL30._

object SphereWorld {
  val NUM_SPHERES = 50
  val spheres: Array[GLFrame] = {
    val res = new Array[GLFrame](NUM_SPHERES)
    for (i <- 0 until res.length) {
      res(i) = new GLFrame
    }
    res
  }

  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val viewFrustum = new GLFrustum
  val transformPipeline = new GLGeometryTransform
  val cameraFrame = new GLFrame

  val torusBatch = new GLTriangleBatch
  val floorBatch = new GLBatch
  val sphereBatch = new GLTriangleBatch

  val uiTextures = new IntArray(3)

  // Called to draw dancing objects
  val vWhite = M3DVector(1.0f, 1.0f, 1.0f, 1.0f)
  val vLightPos = M3DVector(0.0f, 3.0f, 0.0f, 1.0f);

  def DrawSongAndDance(yRot: Float) {
    // Get the light position in eye space
    val vLightTransformed = new M3DVector4f
    val mCamera = new M3DMatrix44f
    modelViewMatrix.GetMatrix(mCamera);
    m3dTransformVector4(vLightTransformed, vLightPos, mCamera);

    // Draw the light source
    modelViewMatrix.PushMatrix();
    modelViewMatrix.Translatev(vLightPos);
    shaderManager.UseStockShader(GLT_SHADER_FLAT,
      transformPipeline.GetModelViewProjectionMatrix(),
      vWhite);
    sphereBatch.Draw();
    modelViewMatrix.PopMatrix();

    glBindTexture(GL_TEXTURE_2D, uiTextures(2));
    var i = 0
    while (i < NUM_SPHERES) {
      modelViewMatrix.PushMatrix();
      modelViewMatrix.MultMatrix(spheres(i));
      shaderManager.UseStockShader(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF,
        modelViewMatrix.GetMatrix(),
        transformPipeline.GetProjectionMatrix(),
        vLightTransformed,
        vWhite,
        0);
      sphereBatch.Draw();
      modelViewMatrix.PopMatrix();
      i += 1
    }

    // Song and dance
    modelViewMatrix.Translate(0.0f, 0.2f, -2.5f);
    modelViewMatrix.PushMatrix(); // Saves the translated origin
    modelViewMatrix.Rotate(yRot, 0.0f, 1.0f, 0.0f);

    // Draw stuff relative to the camera
    glBindTexture(GL_TEXTURE_2D, uiTextures(1));
    shaderManager.UseStockShader(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF,
      modelViewMatrix.GetMatrix(),
      transformPipeline.GetProjectionMatrix(),
      vLightTransformed,
      vWhite,
      0);
    torusBatch.Draw();
    modelViewMatrix.PopMatrix(); // Erased the rotate

    modelViewMatrix.Rotate(yRot * -2.0f, 0.0f, 1.0f, 0.0f);
    modelViewMatrix.Translate(0.8f, 0.0f, 0.0f);

    glBindTexture(GL_TEXTURE_2D, uiTextures(2));
    shaderManager.UseStockShader(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF,
      modelViewMatrix.GetMatrix(),
      transformPipeline.GetProjectionMatrix(),
      vLightTransformed,
      vWhite,
      0);
    sphereBatch.Draw();
  }


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


  //////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    // Make sure OpenGL entry points are set
    //	glewInit();

    // Initialze Shader Manager
    shaderManager.InitializeStockShaders();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    // This makes a torus
    gltMakeTorus(torusBatch, 0.4f, 0.15f, 40, 20);

    // This makes a sphere
    gltMakeSphere(sphereBatch, 0.1f, 26, 13);


    // Make the solid ground
    val texSize = 10.0f;
    floorBatch.Begin(GL_TRIANGLE_FAN, 4, 1);
    floorBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    floorBatch.Vertex3f(-20.0f, -0.41f, 20.0f);

    floorBatch.MultiTexCoord2f(0, texSize, 0.0f);
    floorBatch.Vertex3f(20.0f, -0.41f, 20.0f);

    floorBatch.MultiTexCoord2f(0, texSize, texSize);
    floorBatch.Vertex3f(20.0f, -0.41f, -20.0f);

    floorBatch.MultiTexCoord2f(0, 0.0f, texSize);
    floorBatch.Vertex3f(-20.0f, -0.41f, -20.0f);
    floorBatch.End();

    // Make 3 texture objects
    glGenTextures(uiTextures);

    // Load the Marble
    glBindTexture(GL_TEXTURE_2D, uiTextures(0));
    LoadTGATexture("marble.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_REPEAT);

    // Load Mars
    glBindTexture(GL_TEXTURE_2D, uiTextures(1));
    LoadTGATexture("marslike.tga", GL_LINEAR_MIPMAP_LINEAR,
      GL_LINEAR, GL_CLAMP_TO_EDGE);

    // Load Moon
    glBindTexture(GL_TEXTURE_2D, uiTextures(2));
    LoadTGATexture("moonlike.tga", GL_LINEAR_MIPMAP_LINEAR,
      GL_LINEAR, GL_CLAMP_TO_EDGE);

    // Randomly place the spheres
    val rand = new scala.util.Random
    for (i <- 0 until NUM_SPHERES) {
      val x = ((rand.nextFloat * 400) - 200).toFloat * 0.1f
      val z = ((rand.nextFloat * 400) - 200).toFloat * 0.1f
      spheres(i).SetOrigin(x, 0.0f, z);
    }
  }

  ////////////////////////////////////////////////////////////////////////
  // Do shutdown for the rendering context
  def ShutdownRC() {
    glDeleteTextures(uiTextures);
  }

  // Called to draw scene
  val rotTimer = new CStopWatch
  val mCamera = new M3DMatrix44f;
  val vFloorColor = M3DVector(1.0f, 1.0f, 1.0f, 0.75f);

  def RenderScene() {
    val yRot = rotTimer.GetElapsedSeconds() * 60.0f;

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    modelViewMatrix.PushMatrix();
    cameraFrame.GetCameraMatrix(mCamera);
    modelViewMatrix.MultMatrix(mCamera);

    // Draw the world upside down
    modelViewMatrix.PushMatrix();
    modelViewMatrix.Scale(1.0f, -1.0f, 1.0f); // Flips the Y Axis
    modelViewMatrix.Translate(0.0f, 0.8f, 0.0f); // Scootch the world down a bit...
    glFrontFace(GL_CW);
    DrawSongAndDance(yRot);
    glFrontFace(GL_CCW);
    modelViewMatrix.PopMatrix();

    // Draw the solid ground
    glEnable(GL_BLEND);
    glBindTexture(GL_TEXTURE_2D, uiTextures(0));
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    shaderManager.UseStockShader(GLT_SHADER_TEXTURE_MODULATE,
      transformPipeline.GetModelViewProjectionMatrix(),
      vFloorColor,
      0);

    floorBatch.Draw();
    glDisable(GL_BLEND);


    DrawSongAndDance(yRot);

    modelViewMatrix.PopMatrix();


    // Do the buffer Swap
    glutSwapBuffers();

    // Do it again
    glutPostRedisplay();
  }


  // Respond to arrow keys by moving the camera frame of reference
  def SpecialKeys(key: Int, x: Int, y: Int) {
    val linear = 0.1f;
    val angular = m3dDegToRad(5.0f).toFloat

    if (key == GLUT_KEY_UP)
      cameraFrame.MoveForward(linear);

    if (key == GLUT_KEY_DOWN)
      cameraFrame.MoveForward(-linear);

    if (key == GLUT_KEY_LEFT)
      cameraFrame.RotateWorld(angular, 0.0f, 1.0f, 0.0f);

    if (key == GLUT_KEY_RIGHT)
      cameraFrame.RotateWorld(-angular, 0.0f, 1.0f, 0.0f);
  }


  def ChangeSize(nWidth: Int, nHeight: Int) {
    glViewport(0, 0, nWidth, nHeight);
    transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);

    viewFrustum.SetPerspective(35.0f, nWidth.toFloat / nHeight.toFloat, 1.0f, 100.0f);
    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    modelViewMatrix.LoadIdentity();
  }

  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(800, 600);

    glutCreateWindow("OpenGL SphereWorld");

    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);
    glutSpecialFunc(SpecialKeys);

    SetupRC();
    glutMainLoop();
    ShutdownRC();
  }
}
