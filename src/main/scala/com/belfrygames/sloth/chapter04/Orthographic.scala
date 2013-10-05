package com.belfrygames.sloth.chapter04

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

object Orthographic {
  val viewFrame = new GLFrame
  val viewFrustum = new GLFrustum
  val tubeBatch = new GLBatch
  val innerBatch  = new GLBatch
  val modelViewMatix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val transformPipeline = new GLGeometryTransform
  val shaderManager = GLShaderManager

  // Called to draw scene
  val vRed = M3DVector(1.0f, 0.0f, 0.0f, 1.0f)
  val vGray = M3DVector(0.75f, 0.75f, 0.75f, 1.0f)
  def RenderScene() {
		// Clear the window and the depth buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

//    glEnable(GL_CULL_FACE);
    glEnable(GL_DEPTH_TEST);


    modelViewMatix.PushMatrix(viewFrame);

    shaderManager.UseStockShader(GLT_SHADER_DEFAULT_LIGHT, transformPipeline.GetModelViewMatrix(), transformPipeline.GetProjectionMatrix(), vRed);
    tubeBatch.Draw();


    shaderManager.UseStockShader(GLT_SHADER_DEFAULT_LIGHT, transformPipeline.GetModelViewMatrix(), transformPipeline.GetProjectionMatrix(), vGray);
    innerBatch.Draw();

    modelViewMatix.PopMatrix();


    glutSwapBuffers();
  }

  // This function does any needed initialization on the rendering
  // context.
  def SetupRC() {
		// Black background
		glClearColor(0.0f, 0.0f, 0.75f, 1.0f );

//    glEnable(GL_CULL_FACE);
    glEnable(GL_DEPTH_TEST);

    shaderManager.InitializeStockShaders();


    tubeBatch.Begin(GL_QUADS, 200);

    val fZ = 100.0f;
    val bZ = -100.0f;

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, 100.0f);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f,50.0f,fZ);

    // Right Panel
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(50.0f,-50.0f,fZ);

    // Top Panel
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, 35.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 35.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 50.0f,fZ);

    // Bottom Panel
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -35.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -35.0f,fZ);

    // Top length section ////////////////////////////
    // Normal points up Y axis
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f,50.0f,bZ);

    // Bottom section
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, fZ);

    // Left section
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, bZ);

    // Right Section
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, bZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);


    // Pointing straight out Z
    // Left Panel
    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, fZ);

    tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f,50.0f,fZ);

    // Right Panel
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(50.0f,-50.0f,fZ);

    // Top Panel
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, 35.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 35.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, 50.0f,fZ);

    // Bottom Panel
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -35.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(-35.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
    tubeBatch.Vertex3f(35.0f, -35.0f,fZ);

    // Top length section ////////////////////////////
    // Normal points up Y axis
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f,50.0f,bZ);

    // Bottom section
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, fZ);

    // Left section
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, -50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(50.0f, 50.0f, bZ);

    // Right Section
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, fZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, 50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, bZ);

		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
    tubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
    tubeBatch.Vertex3f(-50.0f, -50.0f, fZ);



		// Left Panel
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f,50.0f,bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f, -50.0f, bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-50.0f, -50.0f, bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-50.0f, 50.0f, bZ);

		// Right Panel
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);

		tubeBatch.Vertex3f(50.0f,-50.0f,bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);

		tubeBatch.Vertex3f(35.0f, -50.0f, bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);

		tubeBatch.Vertex3f(35.0f, 50.0f, bZ);

		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);

		tubeBatch.Vertex3f(50.0f, 50.0f, bZ);

		// Top Panel
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(35.0f, 50.0f, bZ);
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(35.0f, 35.0f, bZ);
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f, 35.0f, bZ);


		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f, 50.0f, bZ);

		// Bottom Panel
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(35.0f, -35.0f,bZ);
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(35.0f, -50.0f, bZ);
		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f, -50.0f, bZ);


		tubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
		tubeBatch.Color4f(1.0f, 0.0f, 0.0f, 1.0f);
		tubeBatch.Vertex3f(-35.0f, -35.0f, bZ);

		tubeBatch.End();


		innerBatch.Begin(GL_QUADS, 40);



		// Insides /////////////////////////////
		// Normal points up Y axis
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, 35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, 35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, 35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f,35.0f,bZ);

		// Bottom section
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, -35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, -35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, -35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(0.0f, 1.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, -35.0f, fZ);

		// Left section
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, 35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, 35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, -35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(-35.0f, -35.0f, fZ);

		// Right Section
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(-1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, 35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(-1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, -35.0f, fZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(-1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, -35.0f, bZ);
		innerBatch.Color4f(0.75f, 0.75f, 0.75f, 1.0f);
		innerBatch.Normal3f(-1.0f, 0.0f, 0.0f);
		innerBatch.Vertex3f(35.0f, 35.0f, bZ);

		innerBatch.End();

  }

  def SpecialKeys(key : Int, x : Int, y : Int) {
		if(key == GLUT_KEY_UP)
			viewFrame.RotateWorld(m3dDegToRad(-5.0).toFloat, 1.0f, 0.0f, 0.0f);

		if(key == GLUT_KEY_DOWN)
			viewFrame.RotateWorld(m3dDegToRad(5.0).toFloat, 1.0f, 0.0f, 0.0f);

		if(key == GLUT_KEY_LEFT)
			viewFrame.RotateWorld(m3dDegToRad(-5.0).toFloat, 0.0f, 1.0f, 0.0f);

		if(key == GLUT_KEY_RIGHT)
			viewFrame.RotateWorld(m3dDegToRad(5.0).toFloat, 0.0f, 1.0f, 0.0f);

		// Refresh the Window
		glutPostRedisplay();
  }

  def ChangeSize(w : Int, _h : Int) {
		// Prevent a divide by zero
		val h = if(_h == 0) 1 else _h

		// Set Viewport to window dimensions
    glViewport(0, 0, w, h);

    viewFrustum.SetOrthographic(-130.0f, 130.0f, -130.0f, 130.0f, -130.0f, 130.0f);

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
		glutCreateWindow("Orthographic Projection Example");
    glutReshapeFunc(ChangeSize);
    glutSpecialFunc(SpecialKeys);
    glutDisplayFunc(RenderScene);


//	GLenum err = glewInit();
//	if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//    }

		SetupRC();

		glutMainLoop();
  }
}
