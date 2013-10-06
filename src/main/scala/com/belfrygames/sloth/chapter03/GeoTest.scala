package com.belfrygames.sloth.chapter03

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object GeoTest {
  val viewFrame = new GLFrame;
  val viewFrustum = new GLFrustum;

  val torusBatch = new GLTriangleBatch;
  val modelViewMatix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  // Flags for effects
  var iCull = false;
  var iDepth = false;

  ///////////////////////////////////////////////////////////////////////////////
  // Reset flags as appropriate in response to menu selections
  def ProcessMenu(value: Int) {
    value match {
      case 1 => iDepth = !iDepth;
      case 2 => iCull = !iCull;
      case 3 => glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
      case 4 => glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
      case 5 => glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
    }

    glutPostRedisplay();
  }


  // Called to draw scene
  def RenderScene() {
    // Clear the window and the depth buffer
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Turn culling on if flag is set
    if (iCull)
      glEnable(GL_CULL_FACE);
    else
      glDisable(GL_CULL_FACE);

    // Enable depth testing if flag is set
    if (iDepth)
      glEnable(GL_DEPTH_TEST);
    else
      glDisable(GL_DEPTH_TEST);


    modelViewMatix.PushMatrix(viewFrame);

    val vRed = M3DVector(1.0f, 0.0f, 0.0f, 1.0f)
    //shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vRed);
    shaderManager.UseStockShader(GLT_SHADER_DEFAULT_LIGHT, transformPipeline.GetModelViewMatrix(), transformPipeline.GetProjectionMatrix(), vRed);


    torusBatch.Draw();

    modelViewMatix.PopMatrix();


    glutSwapBuffers();
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
    // Black background
    glClearColor(0.3f, 0.3f, 0.3f, 1.0f);

    shaderManager.InitializeStockShaders();
    viewFrame.MoveForward(7.0f);

    // Make the torus
    gltMakeTorus(torusBatch, 1.0f, 0.3f, 52, 26);

    glPointSize(4.0f);
  }

  def SpecialKeys(key: Int, x: Int, y: Int) {
    if (key == GLUT_KEY_UP)
      viewFrame.RotateWorld(m3dDegToRad(-5.0).toFloat, 1.0f, 0.0f, 0.0f);

    if (key == GLUT_KEY_DOWN)
      viewFrame.RotateWorld(m3dDegToRad(5.0).toFloat, 1.0f, 0.0f, 0.0f);

    if (key == GLUT_KEY_LEFT)
      viewFrame.RotateWorld(m3dDegToRad(-5.0).toFloat, 0.0f, 1.0f, 0.0f);

    if (key == GLUT_KEY_RIGHT)
      viewFrame.RotateWorld(m3dDegToRad(5.0).toFloat, 0.0f, 1.0f, 0.0f);

    // Refresh the Window
    glutPostRedisplay();
  }


  def ChangeSize(w: Int, _h: Int) {
    // Prevent a divide by zero
    val h = if (_h == 0) 1 else _h

    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 100.0f);

    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    transformPipeline.SetMatrixStacks(modelViewMatix, projectionMatrix);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
    if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
    glutInitWindowSize(800, 600);
    glutCreateWindow("Geometry Test Program");
    glutReshapeFunc(ChangeSize);

    glutSpecialFunc((key: Int, x: Int, y: Int) => key match {
      case GLUT_KEY_F1 => ProcessMenu(1)
      case GLUT_KEY_F2 => ProcessMenu(2)
      case GLUT_KEY_F3 => ProcessMenu(3)
      case GLUT_KEY_F4 => ProcessMenu(4)
      case GLUT_KEY_F5 => ProcessMenu(5)
      case _ => SpecialKeys(key, x, y)
    })

    glutDisplayFunc(RenderScene);

    // Create the Menu
    //    glutCreateMenu(ProcessMenu);
    //    glutAddMenuEntry("Toggle depth test",1);
    //    glutAddMenuEntry("Toggle cull backface",2);
    //    glutAddMenuEntry("Set Fill Mode", 3);
    //    glutAddMenuEntry("Set Line Mode", 4);
    //    glutAddMenuEntry("Set Point Mode", 5);
    //
    //    glutAttachMenu(GLUT_RIGHT_BUTTON);


    //	GLenum err = glewInit();
    //	if (GLEW_OK != err) {
    //	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
    //	  return 1;
    //    }

    SetupRC();

    glutMainLoop();
  }
}
