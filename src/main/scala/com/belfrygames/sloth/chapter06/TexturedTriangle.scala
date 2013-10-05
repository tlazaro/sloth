package com.belfrygames.sloth.chapter06

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.Math3D.M3DVector._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL32._

object TexturedTriangle {
  import GLBatch._

  val triangleBatch = new GLBatch
  val shaderManager = GLShaderManager

  var myTexturedIdentityShader = 0
  var textureID = 0

///////////////////////////////////////////////////////////////////////////////
// Window has changed size, or has just been created. In either case, we need
// to use the window dimensions to set the viewport and the projection matrix.
  def ChangeSize(w : Int, h : Int)
  {
	glViewport(0, 0, w, h);
  }


// Load a TGA as a 2D Texture. Completely initialize the state
  def LoadTGATexture(szFileName : String, minFilter : Int, magFilter : Int, wrapMode : Int): Boolean =
  {
	// Read the texture bits
	val (pBits, nWidth, nHeight, nComponents, eFormat) = gltReadTGABits(szFileName)

	if(pBits == null)
	  return false;

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);

	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

	glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	glTexImage2D(GL_TEXTURE_2D, 0, nComponents, nWidth, nHeight, 0,
				 eFormat, GL_UNSIGNED_BYTE, pBits);

    if(minFilter == GL_LINEAR_MIPMAP_LINEAR ||
       minFilter == GL_LINEAR_MIPMAP_NEAREST ||
       minFilter == GL_NEAREST_MIPMAP_LINEAR ||
       minFilter == GL_NEAREST_MIPMAP_NEAREST)
		 glGenerateMipmap(GL_TEXTURE_2D);

	return true;
  }


///////////////////////////////////////////////////////////////////////////////
// This function does any needed initialization on the rendering context.
// This is the first opportunity to do any OpenGL related tasks.
  def SetupRC() {
	// Blue background
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f );

	shaderManager.InitializeStockShaders();

	// Load up a triangle
	val vVerts = Array[Float]( -0.5f, 0.0f, 0.0f,
							  0.5f, 0.0f, 0.0f,
							  0.0f, 0.5f, 0.0f )

	val vTexCoords = Array[Float]( 0.0f, 0.0f,
								  1.0f, 0.0f,
								  0.5f, 1.0f )

	triangleBatch.Begin(GL_TRIANGLES, 3, 1);
	triangleBatch.CopyVertexData3f(vVerts);
	triangleBatch.CopyTexCoordData2f(vTexCoords, 0);
	triangleBatch.End();

	myTexturedIdentityShader = gltLoadShaderPairWithAttributes("TexturedIdentity.vp", "TexturedIdentity.fp", 2,
													   GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "vTexCoords");

	textureID = glGenTextures();
	glBindTexture(GL_TEXTURE_2D, textureID);
	LoadTGATexture("stone.tga", GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Cleanup
  def ShutdownRC() {
	glDeleteProgram(myTexturedIdentityShader);
	glDeleteTextures(textureID);
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Called to draw scene
  def RenderScene() {
	// Clear the window with current clearing color
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

	glUseProgram(myTexturedIdentityShader);
    glBindTexture(GL_TEXTURE_2D, textureID);
    val iTextureUniform = glGetUniformLocation(myTexturedIdentityShader, "colorMap");
	glUniform1i(iTextureUniform, 0);

	triangleBatch.Draw();

	// Perform the buffer swap to display back buffer
	glutSwapBuffers();
  }


///////////////////////////////////////////////////////////////////////////////
// Main entry point for GLUT based programs
  def main(args: Array[String]): Unit = {
	if (args.size > 0) gltSetWorkingDirectory(args(0))

	glutInit(args);
	glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH | GLUT_STENCIL);
	glutInitWindowSize(800, 600);
	glutCreateWindow("Textured Triangle");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);

//	GLenum err = glewInit();
//	if (GLEW_OK != err) {
//	  fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//	  return 1;
//	}

	SetupRC();

	glutMainLoop();

	ShutdownRC();
  }
}
