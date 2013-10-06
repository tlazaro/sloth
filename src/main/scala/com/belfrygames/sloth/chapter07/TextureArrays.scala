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

object TextureArrays {

  import GLBatch._

  val shaderManager = GLShaderManager
  val viewFrustum = new GLFrustum
  val smallStarBatch = new GLBatch
  val mediumStarBatch = new GLBatch
  val largeStarBatch = new GLBatch
  val mountainRangeBatch = new GLBatch
  val moonBatch = new GLBatch

  var starTexture = 0
  var starFieldShader = 0
  // The point sprite shader
  var locMVP = 0
  // The location of the ModelViewProjection matrix uniform
  var locStarTexture = 0 // The location of the  texture uniform


  var moonTexture = 0
  var moonShader = 0
  var locMoonMVP = 0
  var locMoonTexture = 0
  var locMoonTime = 0

  var locTimeStamp = 0 // The location of the time stamp


  // Array of small stars
  val SMALL_STARS = 100
  val MEDIUM_STARS = 40
  val LARGE_STARS = 15

  val SCREEN_X = 800
  val SCREEN_Y = 600


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
    glTexImage2D(GL_TEXTURE_2D, 0, nComponents, nWidth, nHeight, 0,
      eFormat, GL_UNSIGNED_BYTE, pBits);

    if (minFilter == GL_LINEAR_MIPMAP_LINEAR ||
      minFilter == GL_LINEAR_MIPMAP_NEAREST ||
      minFilter == GL_NEAREST_MIPMAP_LINEAR ||
      minFilter == GL_NEAREST_MIPMAP_NEAREST)
      glGenerateMipmap(GL_TEXTURE_2D);

    return true;
  }

  ///////////////////////////////////////////////////
  // Called to draw scene
  val vWhite = M3DVector(1.0f, 1.0f, 1.0f, 1.0f)
  lazy val timer = new CStopWatch

  def RenderScene() {

    // Clear the window
    glClear(GL_COLOR_BUFFER_BIT);

    // Everything is white
    glBindTexture(GL_TEXTURE_2D, starTexture);
    glUseProgram(starFieldShader);
    glUniformMatrix4(locMVP, false, viewFrustum.GetProjectionMatrix());
    glUniform1i(locStarTexture, 0);

    // Draw small stars
    glPointSize(4.0f);
    smallStarBatch.Draw();

    // Draw medium sized stars
    glPointSize(8.0f);
    mediumStarBatch.Draw();

    // Draw largest stars
    glPointSize(12.0f);
    largeStarBatch.Draw();

    // Draw distant horizon
    shaderManager.UseStockShader(GLT_SHADER_FLAT, viewFrustum.GetProjectionMatrix(), vWhite);
    glLineWidth(3.5f);
    mountainRangeBatch.Draw();

    // Draw the "moon"
    glBindTexture(GL_TEXTURE_2D_ARRAY, moonTexture);
    glUseProgram(moonShader);
    glUniformMatrix4(locMoonMVP, false, viewFrustum.GetProjectionMatrix());
    glUniform1i(locMoonTexture, 0);

    // fTime goes from 0.0 to 28.0 and recycles
    val fTime = fmod(timer.GetElapsedSeconds(), 28.0f);
    glUniform1f(locTimeStamp, fTime);

    moonBatch.Draw();

    // Swap buffers
    glutSwapBuffers();

    glutPostRedisplay();
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    val vVerts = new M3DVector3fArray(SMALL_STARS) // SMALL_STARS is the largest batch we are going to need

    shaderManager.InitializeStockShaders();

    // Populate star list
    smallStarBatch.Begin(GL_POINTS, SMALL_STARS);
    val rand = new scala.util.Random
    for (i <- 0 until SMALL_STARS) {
      vVerts(i)(0) = rand.nextFloat * SCREEN_X
      vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
      vVerts(i)(2) = 0.0f;
    }
    smallStarBatch.CopyVertexData3f(vVerts);
    smallStarBatch.End();

    // Populate star list
    mediumStarBatch.Begin(GL_POINTS, MEDIUM_STARS);
    for (i <- 0 until MEDIUM_STARS) {
      vVerts(i)(0) = rand.nextFloat * SCREEN_X
      vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
      vVerts(i)(2) = 0.0f;
    }
    mediumStarBatch.CopyVertexData3f(vVerts);
    mediumStarBatch.End();

    // Populate star list
    largeStarBatch.Begin(GL_POINTS, LARGE_STARS);
    for (i <- 0 until LARGE_STARS) {
      vVerts(i)(0) = rand.nextFloat * SCREEN_X
      vVerts(i)(1) = (rand.nextFloat * (SCREEN_Y - 100)) + 100.0f;
      vVerts(i)(2) = 0.0f;
    }
    largeStarBatch.CopyVertexData3f(vVerts);
    largeStarBatch.End();

    val vMountains = Array[Float](0.0f, 25.0f, 0.0f,
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
    val x = 700.0f;
    // Location and radius of moon
    val y = 500.0f;
    val r = 50.0f;
    var angle = 0.0f; // Another looping variable

    moonBatch.Begin(GL_TRIANGLE_FAN, 4, 1);
    moonBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    moonBatch.Vertex3f(x - r, y - r, 0.0f);

    moonBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
    moonBatch.Vertex3f(x + r, y - r, 0.0f);

    moonBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
    moonBatch.Vertex3f(x + r, y + r, 0.0f);

    moonBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
    moonBatch.Vertex3f(x - r, y + r, 0.0f);
    moonBatch.End();

    // Black background
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    glEnable(GL_POINT_SPRITE);

    // Turn on line antialiasing, and give hint to do the best
    // job possible.
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_BLEND);
    glEnable(GL_LINE_SMOOTH);
    glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

    starFieldShader = gltLoadShaderPairWithAttributes("StarField.vp", "StarField.fp", 1, GLT_ATTRIBUTE_VERTEX, "vVertex");

    locMVP = glGetUniformLocation(starFieldShader, "mvpMatrix");
    locStarTexture = glGetUniformLocation(starFieldShader, "starImage");

    moonShader = gltLoadShaderPairWithAttributes("MoonShader.vp", "MoonShader.fp", 2, GLT_ATTRIBUTE_VERTEX, "vVertex",
      GLT_ATTRIBUTE_TEXTURE0, "vTexCoords");
    locMoonMVP = glGetUniformLocation(moonShader, "mvpMatrix");
    locMoonTexture = glGetUniformLocation(moonShader, "moonImage");
    locMoonTime = glGetUniformLocation(moonShader, "fTime");


    starTexture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, starTexture);
    LoadTGATexture("star.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);


    moonTexture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D_ARRAY, moonTexture);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, 64, 64, 30, 0, GL_BGRA, GL_UNSIGNED_BYTE, null.asInstanceOf[java.nio.IntBuffer]);

    for (i <- 0 until 29) {
      val cFile = "moon%02d.tga".format(i)

      // Read the texture bits
      val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(cFile)
      glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, nWidth, nHeight, 1, GL_BGRA, GL_UNSIGNED_BYTE, pBits);
    }
  }

  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetOrthographic(0.0f, SCREEN_X, 0.0f, SCREEN_Y, -1.0f, 1.0f);
  }

  def main(args: Array[String]): Unit = {
    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Texture Arrays");

    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

    //    GLenum err = glewInit();
    //    if (GLEW_OK != err) {
    //			fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //			return 1;
    //		}

    SetupRC();
    glutMainLoop();
  }
}
