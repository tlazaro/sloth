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

object PointSprites {
  val NUM_STARS = 10000

  val viewFrustum = new GLFrustum
  val starsBatch = new GLBatch

  var starFieldShader = 0
  // The point sprite shader
  var locMVP = 0
  // The location of the ModelViewProjection matrix uniform
  var locTimeStamp = 0
  // The location of the time stamp
  var locTexture = 0 // The location of the  texture uniform

  var starTexture = 0 // The star texture texture object


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


  // This function does any needed initialization on the rendering
  // context.
  val fColors = M3DVector4fArray(1.0f, 1.0f, 1.0f, 1.0f, // White
    0.67f, 0.68f, 0.82f, 1.0f, // Blue Stars
    1.0f, 0.5f, 0.5f, 1.0f, // Reddish
    1.0f, 0.82f, 0.65f, 1.0f)

  // Orange
  def SetupRC() {
    // Background
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    glEnable(GL_POINT_SPRITE);

    // Randomly place the stars in their initial positions, and pick a random color

    val rand = new scala.util.Random
    starsBatch.Begin(GL_POINTS, NUM_STARS);
    for (i <- 0 until NUM_STARS) {
      var iColor = 0; // All stars start as white

      // One in five will be blue
      if (rand.nextFloat * 5 <= 1)
        iColor = 1;

      // One in 50 red
      if (rand.nextFloat * 50 <= 1)
        iColor = 2;

      // One in 100 is amber
      if (rand.nextFloat * 100 <= 1)
        iColor = 3;

      starsBatch.Color4fv(fColors(iColor));

      val vPosition = new M3DVector3f
      vPosition(0) = (3000 - (rand.nextFloat * 6000)) * 0.1f;
      vPosition(1) = (3000 - (rand.nextFloat * 6000)) * 0.1f;
      vPosition(2) = -(rand.nextFloat * 1000) - 1.0f; // -1 to -1000.0f

      starsBatch.Vertex3fv(vPosition);
    }
    starsBatch.End();


    starFieldShader = gltLoadShaderPairWithAttributes("SpaceFlight.vp", "SpaceFlight.fp", 2, GLT_ATTRIBUTE_VERTEX, "vVertex",
      GLT_ATTRIBUTE_COLOR, "vColor");

    locMVP = glGetUniformLocation(starFieldShader, "mvpMatrix");
    locTexture = glGetUniformLocation(starFieldShader, "starImage");
    locTimeStamp = glGetUniformLocation(starFieldShader, "timeStamp");

    starTexture = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, starTexture);
    LoadTGATexture("star.tga", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);
  }

  // Cleanup
  def ShutdownRC() {
    glDeleteTextures(starTexture);
  }


  // Called to draw scene
  lazy val timer = new CStopWatch

  def RenderScene() {
    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT);

    // Turn on additive blending
    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE);

    // Let the vertex program determine the point size
    glEnable(GL_PROGRAM_POINT_SIZE);

    // Bind to our shader, set uniforms
    glUseProgram(starFieldShader);
    glUniformMatrix4(locMVP, false, viewFrustum.GetProjectionMatrix());
    glUniform1i(locTexture, 0);

    // fTime goes from 0.0 to 999.0 and recycles
    val fTime = fmod(timer.GetElapsedSeconds() * 10.0f, 999.0f);
    glUniform1f(locTimeStamp, fTime);

    // Draw the stars
    starsBatch.Draw();

    glutSwapBuffers();
    glutPostRedisplay();
  }

  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 1000.0f);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Spaced Out");
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
