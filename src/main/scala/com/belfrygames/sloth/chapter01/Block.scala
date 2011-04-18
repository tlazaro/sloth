package com.belfrygames.sloth.chapter01

import com.belfrygames.sloth._
import com.belfrygames.sloth.IntArray._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.EXTTextureFilterAnisotropic._

object Block {
  // Block.cpp
  // OpenGL SuperBible, Chapter 1
  // Demonstrates an assortment of basic 3D concepts
  // Program by Richard S. Wright Jr.

  /////////////////////////////////////////////////////////////////////////////////
  // An assortment of needed classes
  val shaderManager = GLShaderManager
  val modelViewMatrix = new GLMatrixStack
  val projectionMatrix = new GLMatrixStack
  val cameraFrame = new GLFrame
  val viewFrustum = new GLFrustum

  val cubeBatch = new GLBatch
  val floorBatch = new GLBatch
  val topBlock = new GLBatch
  val frontBlock = new GLBatch
  val leftBlock = new GLBatch

  val transformPipeline = new GLGeometryTransform
  val shadowMatrix = new M3DMatrix44f

  // Keep track of effects step
  var nStep = 0

  // Lighting data
  val lightAmbient = M3DVector( 0.2f, 0.2f, 0.2f, 1.0f )
  val lightDiffuse = M3DVector( 0.7f, 0.7f, 0.7f, 1.0f )
  val lightSpecular = M3DVector( 0.9f, 0.9f, 0.9f )
  val vLightPos = M3DVector( -8.0f, 20.0f, 100.0f, 1.0f )

  val textures = new IntArray(4)

  ///////////////////////////////////////////////////////////////////////////////
  // Make a cube out of a batch of triangles. Texture coordinates and normals
  // are also provided.
  def MakeCube(cubeBatch : GLBatch) {
	cubeBatch.Begin(GL_TRIANGLES, 36, 1);

	/////////////////////////////////////////////
	// Top of cube
	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, 1.0f);


	////////////////////////////////////////////
	// Bottom of cube
	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, -1.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	///////////////////////////////////////////
	// Left side of cube
	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(-1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, 1.0f);

	// Right side of cube
	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	cubeBatch.Normal3f(1.0f, 0.0f, 0.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	// Front and Back
	// Front
	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, 1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, 1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, 1.0f);

	// Back
	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	cubeBatch.Vertex3f(-1.0f, -1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	cubeBatch.Vertex3f(-1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	cubeBatch.Vertex3f(1.0f, 1.0f, -1.0f);

	cubeBatch.Normal3f(0.0f, 0.0f, -1.0f);
	cubeBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	cubeBatch.Vertex3f(1.0f, -1.0f, -1.0f);

	cubeBatch.End();
  }

  /////////////////////////////////////////////////////////////////////////////
  // Make the floor, just the verts and texture coordinates, no normals
  def MakeFloor(floorBatch : GLBatch) {
	val x = 5.0f;
    val y = -1.0f;

	floorBatch.Begin(GL_TRIANGLE_FAN, 4, 1);
	floorBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
	floorBatch.Vertex3f(-x, y, x);

	floorBatch.MultiTexCoord2f(0, 1.0f, 0.0f);
	floorBatch.Vertex3f(x, y, x);

	floorBatch.MultiTexCoord2f(0, 1.0f, 1.0f);
	floorBatch.Vertex3f(x, y, -x);

	floorBatch.MultiTexCoord2f(0, 0.0f, 1.0f);
	floorBatch.Vertex3f(-x, y, -x);
	floorBatch.End();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // This function does any needed initialization on the rendering context.
  // This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
	shaderManager.InitializeStockShaders();

	// Black background
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f );
	glEnable(GL_DEPTH_TEST);
	glLineWidth(2.5f);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

	transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);

	cameraFrame.MoveForward(-15.0f);
	cameraFrame.MoveUp(6.0f);
	cameraFrame.RotateLocalX(m3dDegToRad(20.0f).toFloat);

	MakeCube(cubeBatch);
	MakeFloor(floorBatch);

	// Make top
	topBlock.Begin(GL_TRIANGLE_FAN, 4, 1);
	topBlock.Normal3f(0.0f, 1.0f, 0.0f);
	topBlock.MultiTexCoord2f(0, 0.0f, 0.0f);
	topBlock.Vertex3f(-1.0f, 1.0f, 1.0f);

	topBlock.Normal3f(0.0f, 1.0f, 0.0f);
	topBlock.MultiTexCoord2f(0, 1.0f, 0.0f);
	topBlock.Vertex3f(1.0f, 1.0f, 1.0f);

	topBlock.Normal3f(0.0f, 1.0f, 0.0f);
	topBlock.MultiTexCoord2f(0, 1.0f, 1.0f);
	topBlock.Vertex3f(1.0f, 1.0f, -1.0f);

	topBlock.Normal3f(0.0f, 1.0f, 0.0f);
	topBlock.MultiTexCoord2f(0, 0.0f, 1.0f);
	topBlock.Vertex3f(-1.0f, 1.0f, -1.0f);
	topBlock.End();

	// Make Front
	frontBlock.Begin(GL_TRIANGLE_FAN, 4, 1);
	frontBlock.Normal3f(0.0f, 0.0f, 1.0f);
	frontBlock.MultiTexCoord2f(0, 0.0f, 0.0f);
	frontBlock.Vertex3f(-1.0f, -1.0f, 1.0f);

	frontBlock.Normal3f(0.0f, 0.0f, 1.0f);
	frontBlock.MultiTexCoord2f(0, 1.0f, 0.0f);
	frontBlock.Vertex3f(1.0f, -1.0f, 1.0f);

	frontBlock.Normal3f(0.0f, 0.0f, 1.0f);
	frontBlock.MultiTexCoord2f(0, 1.0f, 1.0f);
	frontBlock.Vertex3f(1.0f, 1.0f, 1.0f);

	frontBlock.Normal3f(0.0f, 0.0f, 1.0f);
	frontBlock.MultiTexCoord2f(0, 0.0f, 1.0f);
	frontBlock.Vertex3f(-1.0f, 1.0f, 1.0f);
	frontBlock.End();

	// Make left
	leftBlock.Begin(GL_TRIANGLE_FAN, 4, 1);
	leftBlock.Normal3f(-1.0f, 0.0f, 0.0f);
	leftBlock.MultiTexCoord2f(0, 0.0f, 0.0f);
	leftBlock.Vertex3f(-1.0f, -1.0f, -1.0f);

	leftBlock.Normal3f(-1.0f, 0.0f, 0.0f);
	leftBlock.MultiTexCoord2f(0, 1.0f, 0.0f);
	leftBlock.Vertex3f(-1.0f, -1.0f, 1.0f);

	leftBlock.Normal3f(-1.0f, 0.0f, 0.0f);
	leftBlock.MultiTexCoord2f(0, 1.0f, 1.0f);
	leftBlock.Vertex3f(-1.0f, 1.0f, 1.0f);

	leftBlock.Normal3f(-1.0f, 0.0f, 0.0f);
	leftBlock.MultiTexCoord2f(0, 0.0f, 1.0f);
	leftBlock.Vertex3f(-1.0f, 1.0f, -1.0f);
	leftBlock.End();

	// Create shadow projection matrix
	val floorPlane = M3DVector(0.0f, 1.0f, 0.0f, 1.0f);
	m3dMakePlanarShadowMatrix(shadowMatrix, floorPlane, vLightPos);

	// Load up four textures
	glGenTextures(textures);

	// Wood floor
	{
	  val (pBytes, nWidth, nHeight, nComponents, format) = gltReadTGABits("floor.tga")
	  glBindTexture(GL_TEXTURE_2D, textures(0));
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	  glTexImage2D(GL_TEXTURE_2D,0,nComponents,nWidth, nHeight, 0, format, GL_UNSIGNED_BYTE, pBytes);
	}

	// One of the block faces
	{
	  val (pBytes, nWidth, nHeight, nComponents, format) = gltReadTGABits("block4.tga")
	  glBindTexture(GL_TEXTURE_2D, textures(1));
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	  glTexImage2D(GL_TEXTURE_2D,0,nComponents,nWidth, nHeight, 0, format, GL_UNSIGNED_BYTE, pBytes);
	}

	// Another block face
	{
	  val (pBytes, nWidth, nHeight, nComponents, format) = gltReadTGABits("block5.tga")
	  glBindTexture(GL_TEXTURE_2D, textures(2));
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	  glTexImage2D(GL_TEXTURE_2D,0,nComponents,nWidth, nHeight, 0, format, GL_UNSIGNED_BYTE, pBytes);
	}

	// Yet another block face
	{
	  val (pBytes, nWidth, nHeight, nComponents, format) = gltReadTGABits("block6.tga")
	  glBindTexture(GL_TEXTURE_2D, textures(3));
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	  glTexImage2D(GL_TEXTURE_2D,0,nComponents,nWidth, nHeight, 0, format, GL_UNSIGNED_BYTE, pBytes);
	}
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Render the block
  val vRed = M3DVector(1.0f, 0.0f, 0.0f, 1.0f);
  val vWhite = M3DVector(1.0f, 1.0f, 1.0f, 1.0f);
  def RenderBlock() {
	nStep match {
	  // Wire frame
	  case 0 => {
		  glEnable(GL_BLEND);
		  glEnable(GL_LINE_SMOOTH);
		  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vRed);
		  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		  glDisable(GL_CULL_FACE);

		  // Draw the cube
		  cubeBatch.Draw();
		}

		// Wire frame, but not the back side... we also want the block to be in the stencil buffer
	  case 1 => {
		  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vRed);

		  // Draw solid block in stencil buffer
		  // Back face culling prevents the back sides from showing through
		  // The stencil pattern is used to mask when we draw the floor under it
		  // to keep it from showing through.
		  glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		  glEnable(GL_STENCIL_TEST);
		  glStencilFunc(GL_NEVER, 0, 0);
		  glStencilOp(GL_INCR, GL_INCR, GL_INCR);
		  cubeBatch.Draw();
		  glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		  glDisable(GL_STENCIL_TEST);

		  glEnable(GL_BLEND);
		  glEnable(GL_LINE_SMOOTH);
		  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		  // Draw the front side cube
		  cubeBatch.Draw();
		}

		// Solid
	  case 2 => {
		  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vRed);

		  // Draw the cube
		  cubeBatch.Draw();
		}

		// Lit
	  case 3 => {
		  shaderManager.UseStockShader(GLT_SHADER_POINT_LIGHT_DIFF, modelViewMatrix.GetMatrix(),
									   projectionMatrix.GetMatrix(), vLightPos, vRed);

		  // Draw the cube
		  cubeBatch.Draw();
		}

		// Textured & Lit
	  case _ => {
		  glBindTexture(GL_TEXTURE_2D, textures(2));
		  shaderManager.UseStockShader(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF, modelViewMatrix.GetMatrix(),
									   projectionMatrix.GetMatrix(), vLightPos, vWhite, 0);

		  glBindTexture(GL_TEXTURE_2D, textures(1));
		  topBlock.Draw();
		  glBindTexture(GL_TEXTURE_2D, textures(2));
		  frontBlock.Draw();
		  glBindTexture(GL_TEXTURE_2D, textures(3));
		  leftBlock.Draw();

		}
	}

	// Put everything back
	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	glEnable(GL_CULL_FACE);
	glDisable(GL_BLEND);
	glDisable(GL_LINE_SMOOTH);
	glDisable(GL_STENCIL_TEST);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Render the floor
  val vBrown = M3DVector(0.55f, 0.292f, 0.09f, 1.0f)
  val vFloor = M3DVector(1.0f, 1.0f, 1.0f, 0.6f)
  def RenderFloor() {
	nStep match {
	  // Wire frame
	  case 0 => {
		  glEnable(GL_BLEND);
		  glEnable(GL_LINE_SMOOTH);
		  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBrown);
		  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		  glDisable(GL_CULL_FACE);
		}

		// Wire frame, but not the back side.. and only where stencil == 0
	  case 1 => {
		  glEnable(GL_BLEND);
		  glEnable(GL_LINE_SMOOTH);

		  glEnable(GL_STENCIL_TEST);
		  glStencilFunc(GL_EQUAL, 0, 0xff);

		  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBrown);
		  glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		}
		// Solid
	  case 2 | 3 =>  shaderManager.UseStockShader(GLT_SHADER_FLAT, transformPipeline.GetModelViewProjectionMatrix(), vBrown);
		// Textured
	  case _ => {
		  glBindTexture(GL_TEXTURE_2D, textures(0));
		  shaderManager.UseStockShader(GLT_SHADER_TEXTURE_MODULATE, transformPipeline.GetModelViewProjectionMatrix(), vFloor, 0);
		}
	}

	// Draw the floor
	floorBatch.Draw();

	// Put everything back
	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	glEnable(GL_CULL_FACE);
	glDisable(GL_BLEND);
	glDisable(GL_LINE_SMOOTH);
	glDisable(GL_STENCIL_TEST);
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  val mCamera = new M3DMatrix44f
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	modelViewMatrix.PushMatrix();
	cameraFrame.GetCameraMatrix(mCamera);
	modelViewMatrix.MultMatrix(mCamera);

	// Reflection step... draw cube upside down, the floor
	// blended on top of it
	if(nStep == 5) {
	  glDisable(GL_CULL_FACE);
	  modelViewMatrix.PushMatrix();
	  modelViewMatrix.Scale(1.0f, -1.0f, 1.0f);
	  modelViewMatrix.Translate(0.0f, 2.0f, 0.0f);
	  modelViewMatrix.Rotate(35.0f, 0.0f, 1.0f, 0.0f);
	  RenderBlock();
	  modelViewMatrix.PopMatrix();
	  glEnable(GL_BLEND);
	  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	  RenderFloor();
	  glDisable(GL_BLEND);
	}


	modelViewMatrix.PushMatrix();

	// Draw normally
	modelViewMatrix.Rotate(35.0f, 0.0f, 1.0f, 0.0f);
	RenderBlock();
	modelViewMatrix.PopMatrix();


	// If not the reflection pass, draw floor last
	if(nStep != 5)
	  RenderFloor();


	modelViewMatrix.PopMatrix();


	// Flush drawing commands
	glutSwapBuffers();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // A normal ASCII key has been pressed.
  // In this case, advance the scene when the space bar is pressed
  def KeyPressFunc(key : Int, x : Int, y : Int)
  {
	if(key == 32)
	{
	  nStep += 1;

	  if(nStep > 5)
		nStep = 0;
	}

	// Refresh the Window
	glutPostRedisplay();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Window has changed size, or has just been created. In either case, we need
  // to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int) {
	glViewport(0, 0, w, h);
	viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 500.0f);
	projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
	modelViewMatrix.LoadIdentity();
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

    glutInit(args)
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
	glutInitWindowSize(800, 600);
	glutCreateWindow("3D Effects Demo");

//	GLenum err = glewInit();
//	if (GLEW_OK != err)
//	{
//	  /* Problem: glewInit failed, something is seriously wrong. */
//	  fprintf(stderr, "Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	glutReshapeFunc(ChangeSize);
	glutKeyboardFunc(KeyPressFunc);
	glutDisplayFunc(RenderScene);

	SetupRC();

	glutMainLoop();
	glDeleteTextures(textures);
	return 0;
  }
}
