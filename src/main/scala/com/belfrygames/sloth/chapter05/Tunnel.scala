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

object Tunnel {
  // Tunnel.cpp
  // Demonstrates mipmapping and using texture objects
  // OpenGL SuperBible
  // Richard S. Wright Jr.
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val viewFrustum = new GLFrustum
  val transformPipeline = new GLGeometryTransform

  val floorBatch = new GLBatch;
  val ceilingBatch = new GLBatch
  val leftWallBatch = new GLBatch
  val rightWallBatch = new GLBatch

  var viewZ = -65.0f;

  // Texture objects
  val TEXTURE_BRICK = 0
  val TEXTURE_FLOOR = 1
  val TEXTURE_CEILING = 2
  val TEXTURE_COUNT = 3

  val textures = new IntArray(TEXTURE_COUNT)
  val szTextureFiles = Array("brick.tga", "floor.tga", "ceiling.tga")

  ///////////////////////////////////////////////////////////////////////////////
  // Change texture filter for each texture object
  def ProcessMenu(value: Int) {
    for (iLoop <- 0 until TEXTURE_COUNT) {
      glBindTexture(GL_TEXTURE_2D, textures(iLoop));

      value match {
        case 0 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        case 1 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        case 2 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        case 3 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        case 4 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        case 5 => glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
      }
    }

    // Trigger Redraw
    glutPostRedisplay();
  }

  //////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering
  // context.  Here it sets up and initializes the texture objects.
  def SetupRC() {
    // Black background
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    shaderManager.InitializeStockShaders();

    // Load textures
    glGenTextures(textures);
    for (iLoop <- 0 until TEXTURE_COUNT) {
      // Bind to next texture object
      glBindTexture(GL_TEXTURE_2D, textures(iLoop));

      // Load texture, set filter and wrap modes
      val (pBytes, iWidth, iHeight, iComponents, eFormat) = gltReadTGABits(szTextureFiles(iLoop))

      // Load texture, set filter and wrap modes
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexImage2D(GL_TEXTURE_2D, 0, iComponents, iWidth, iHeight, 0, eFormat, GL_UNSIGNED_BYTE, pBytes);
      glGenerateMipmap(GL_TEXTURE_2D);
    }

    // Build Geometry
    var z = 60.0f;
    floorBatch.Begin(GL_TRIANGLE_STRIP, 28, 1);
    while (z >= 0.0f) {
      floorBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
      floorBatch.Vertex3f(-10.0f, -10.0f, z);

      floorBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
      floorBatch.Vertex3f(10.0f, -10.0f, z);

      floorBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
      floorBatch.Vertex3f(-10.0f, -10.0f, z - 10.0f);

      floorBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
      floorBatch.Vertex3f(10.0f, -10.0f, z - 10.0f);
      z -= 10.0f
    }
    floorBatch.End();

    ceilingBatch.Begin(GL_TRIANGLE_STRIP, 28, 1);
    z = 60.0f;
    while (z >= 0.0f) {
      ceilingBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
      ceilingBatch.Vertex3f(-10.0f, 10.0f, z - 10.0f);

      ceilingBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
      ceilingBatch.Vertex3f(10.0f, 10.0f, z - 10.0f);

      ceilingBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
      ceilingBatch.Vertex3f(-10.0f, 10.0f, z);

      ceilingBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
      ceilingBatch.Vertex3f(10.0f, 10.0f, z);
      z -= 10.0f
    }
    ceilingBatch.End();

    leftWallBatch.Begin(GL_TRIANGLE_STRIP, 28, 1);
    z = 60.0f;
    while (z >= 0.0f) {
      leftWallBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
      leftWallBatch.Vertex3f(-10.0f, -10.0f, z);

      leftWallBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
      leftWallBatch.Vertex3f(-10.0f, 10.0f, z);

      leftWallBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
      leftWallBatch.Vertex3f(-10.0f, -10.0f, z - 10.0f);

      leftWallBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
      leftWallBatch.Vertex3f(-10.0f, 10.0f, z - 10.0f);
      z -= 10.0f
    }
    leftWallBatch.End();


    rightWallBatch.Begin(GL_TRIANGLE_STRIP, 28, 1);
    z = 60.0f;
    while (z >= 0.0f) {
      rightWallBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
      rightWallBatch.Vertex3f(10.0f, -10.0f, z);

      rightWallBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
      rightWallBatch.Vertex3f(10.0f, 10.0f, z);

      rightWallBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
      rightWallBatch.Vertex3f(10.0f, -10.0f, z - 10.0f);

      rightWallBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
      rightWallBatch.Vertex3f(10.0f, 10.0f, z - 10.0f);
      z -= 10.0f
    }
    rightWallBatch.End();
  }

  ///////////////////////////////////////////////////
  // Shutdown the rendering context. Just deletes the
  // texture objects
  def ShutdownRC() {
    glDeleteTextures(textures);
  }


  ///////////////////////////////////////////////////
  // Respond to arrow keys, move the viewpoint back
  // and forth
  def SpecialKeys(key: Int, x: Int, y: Int) {
    if (key == GLUT_KEY_UP)
      viewZ += 0.5f;

    if (key == GLUT_KEY_DOWN)
      viewZ -= 0.5f;

    // Refresh the Window
    glutPostRedisplay();
  }

  /////////////////////////////////////////////////////////////////////
  // Change viewing volume and viewport.  Called when window is resized
  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    val fAspect = w.toFloat / h.toFloat;

    // Produce the perspective projection
    viewFrustum.SetPerspective(80.0f, fAspect, 1.0f, 120.0f);
    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);

  }

  ///////////////////////////////////////////////////////
  // Called to draw scene
  def RenderScene() {
    // Clear the window with current clearing color
    glClear(GL_COLOR_BUFFER_BIT);

    modelViewMatrix.PushMatrix();
    modelViewMatrix.Translate(0.0f, 0.0f, viewZ);

    shaderManager.UseStockShader(GLT_SHADER_TEXTURE_REPLACE, transformPipeline.GetModelViewProjectionMatrix(), 0);

    glBindTexture(GL_TEXTURE_2D, textures(TEXTURE_FLOOR));
    floorBatch.Draw();

    glBindTexture(GL_TEXTURE_2D, textures(TEXTURE_CEILING));
    ceilingBatch.Draw();

    glBindTexture(GL_TEXTURE_2D, textures(TEXTURE_BRICK));
    leftWallBatch.Draw();
    rightWallBatch.Draw();

    modelViewMatrix.PopMatrix();

    // Buffer swap
    glutSwapBuffers();
  }


  //////////////////////////////////////////////////////
  // Program entry point
  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Tunnel");
    glutReshapeFunc(ChangeSize);
    //    glutSpecialFunc(SpecialKeys);
    glutDisplayFunc(RenderScene);

    glutSpecialFunc((key: Int, x: Int, y: Int) => key match {
      case GLUT_KEY_F1 => println("GL_NEAREST"); ProcessMenu(0);
      case GLUT_KEY_F2 => println("GL_LINEAR"); ProcessMenu(1)
      case GLUT_KEY_F3 => println("GL_NEAREST_MIPMAP_NEAREST"); ProcessMenu(2)
      case GLUT_KEY_F4 => println("GL_NEAREST_MIPMAP_LINEAR"); ProcessMenu(3)
      case GLUT_KEY_F5 => println("GL_LINEAR_MIPMAP_NEAREST"); ProcessMenu(4)
      case GLUT_KEY_F6 => println("GL_LINEAR_MIPMAP_LINEAR"); ProcessMenu(5)
      case _ => SpecialKeys(key, x, y)
    })

    // Add menu entries to change filter
    //    glutCreateMenu(ProcessMenu);
    //    glutAddMenuEntry("GL_NEAREST",0);
    //    glutAddMenuEntry("GL_LINEAR",1);
    //    glutAddMenuEntry("GL_NEAREST_MIPMAP_NEAREST",2);
    //    glutAddMenuEntry("GL_NEAREST_MIPMAP_LINEAR", 3);
    //    glutAddMenuEntry("GL_LINEAR_MIPMAP_NEAREST", 4);
    //    glutAddMenuEntry("GL_LINEAR_MIPMAP_LINEAR", 5);
    //
    //    glutAttachMenu(GLUT_RIGHT_BUTTON);

    //    GLenum err = glewInit();
    //    if (GLEW_OK != err) {
    //	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //	  return 1;
    //    }

    // Startup, loop, shutdown
    SetupRC();
    glutMainLoop();
    ShutdownRC();
  }
}
