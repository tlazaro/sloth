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
import org.lwjgl.opengl.GL32._
import org.lwjgl.opengl.GL33._

object TextureRect {
  val NUM_SPHERES = 50
  val spheres = {
    val a = new Array[GLFrame](NUM_SPHERES)
    for (i <- 0 until NUM_SPHERES)
      a(i) = new GLFrame
    a
  }

  val shaderManager = GLShaderManager
  // Shader Manager
  val modelViewMatrix = new GLMatrixStack
  // Modelview Matrix
  val projectionMatrix = new GLMatrixStack
  // Projection Matrix
  val viewFrustum = new GLFrustum
  // View Frustum
  val transformPipeline = new GLGeometryTransform
  // Geometry Transform Pipeline
  val cameraFrame = new GLFrame // Camera frame

  val torusBatch = new GLTriangleBatch
  val sphereBatch = new GLTriangleBatch
  val floorBatch = new GLBatch
  val logoBatch = new GLBatch

  val uiTextures = new IntArray(4)
  var rectReplaceShader = 0
  var locRectMVP = 0
  var locRectTexture = 0


  val vWhite = M3DVector(1.0f, 1.0f, 1.0f, 1.0f);
  val vLightPos = M3DVector(0.0f, 3.0f, 0.0f, 1.0f);

  val vLightTransformed = new M3DVector4f
  val mCamera = new M3DMatrix44f

  // Called to draw dancing objects
  def DrawSongAndDance(yRot: Float) {
    // Get the light position in eye space
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

    glBindTexture(GL_TEXTURE_2D, uiTextures(2))
    for (i <- 0 until NUM_SPHERES) {
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
    glTexImage2D(GL_TEXTURE_2D, 0, GL_COMPRESSED_RGB, nWidth, nHeight, 0, eFormat, GL_UNSIGNED_BYTE, pBits);

    if (minFilter == GL_LINEAR_MIPMAP_LINEAR ||
      minFilter == GL_LINEAR_MIPMAP_NEAREST ||
      minFilter == GL_NEAREST_MIPMAP_LINEAR ||
      minFilter == GL_NEAREST_MIPMAP_NEAREST)
      glGenerateMipmap(GL_TEXTURE_2D);

    return true;
  }


  def LoadTGATextureRect(szFileName: String, minFilter: Int, magFilter: Int, wrapMode: Int): Boolean = {
    // Read the texture bits
    val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(szFileName)

    if (pBits == null)
      return false;

    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_S, wrapMode);
    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_WRAP_T, wrapMode);

    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, minFilter);
    glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, magFilter);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glTexImage2D(GL_TEXTURE_RECTANGLE, 0, nComponents, nWidth, nHeight, 0,
      eFormat, GL_UNSIGNED_BYTE, pBits);

    return true;
  }

  //////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    // Make sure OpenGL entry points are set
    //		glewInit();

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


    val x = 500;
    val y = 155;
    val width = 300;
    val height = 155;
    logoBatch.Begin(GL_TRIANGLE_FAN, 4, 1);

    // Upper left hand corner
    logoBatch.MultiTexCoord2f(0, 0.0f, height);
    logoBatch.Vertex3f(x, y, 0.0f);

    // Lower left hand corner
    logoBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    logoBatch.Vertex3f(x, y - height, 0.0f);

    // Lower right hand corner
    logoBatch.MultiTexCoord2f(0, width, 0.0f);
    logoBatch.Vertex3f(x + width, y - height, 0.0f);

    // Upper righ hand corner
    logoBatch.MultiTexCoord2f(0, width, height);
    logoBatch.Vertex3f(x + width, y, 0.0f);

    logoBatch.End();

    // Make 4 texture objects
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

    // Load the Logo
    glBindTexture(GL_TEXTURE_RECTANGLE, uiTextures(3));
    LoadTGATextureRect("OpenGL-Logo.tga", GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE);

    rectReplaceShader = gltLoadShaderPairWithAttributes("RectReplace.vp", "RectReplace.fp",
      2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "vTexCoord");


    locRectMVP = glGetUniformLocation(rectReplaceShader, "mvpMatrix");
    locRectTexture = glGetUniformLocation(rectReplaceShader, "rectangleImage");


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
  lazy val rotTimer = new CStopWatch
  val vFloorColor = M3DVector(1.0f, 1.0f, 1.0f, 0.75f)

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

    // Render the overlay

    // Creating this matrix really doesn't need to be done every frame. I'll leave it here
    // so all the pertenant code is together
    val mScreenSpace = new M3DMatrix44f
    m3dMakeOrthographicMatrix(mScreenSpace, 0.0f, 800.0f, 0.0f, 600.0f, -1.0f, 1.0f);

    // Turn blending on, and depth testing off
    glEnable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);

    glUseProgram(rectReplaceShader);
    glUniform1i(locRectTexture, 0);
    glUniformMatrix4(locRectMVP, false, mScreenSpace);
    glBindTexture(GL_TEXTURE_RECTANGLE, uiTextures(3));
    logoBatch.Draw();

    // Restore no blending and depth test
    glDisable(GL_BLEND);
    glEnable(GL_DEPTH_TEST);

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

    glutCreateWindow("OpenGL SphereWorld with Texture Rectangle");

    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);
    glutSpecialFunc(SpecialKeys);

    SetupRC();
    glutMainLoop();
    ShutdownRC();
  }
}
