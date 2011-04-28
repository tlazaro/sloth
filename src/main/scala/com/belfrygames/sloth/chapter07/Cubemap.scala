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


object Cubemap {
	val viewFrame = new GLFrame
	val viewFrustum = new GLFrustum
	val sphereBatch = new GLTriangleBatch
	val cubeBatch = new GLBatch
	val modelViewMatrix = new GLMatrixStack
	val projectionMatrix = new GLMatrixStack
	val transformPipeline = new GLGeometryTransform
	
	var cubeTexture = 0
	var reflectionShader = 0
	var skyBoxShader = 0

	var locMVPReflect = 0
	var locMVReflect = 0
	var locNormalReflect = 0
	var locInvertedCamera = 0
	var locMVPSkyBox = 0

	// Six sides of a cube map
	val szCubeFaces = Array("pos_x.tga", "neg_x.tga", "pos_y.tga", "neg_y.tga", "pos_z.tga", "neg_z.tga")

	val cube = Array(GL_TEXTURE_CUBE_MAP_POSITIVE_X,
									 GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
									 GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
									 GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
									 GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
									 GL_TEXTURE_CUBE_MAP_NEGATIVE_Z );

        
	//////////////////////////////////////////////////////////////////
	// This function does any needed initialization on the rendering
	// context. 
	def SetupRC() {
    // Cull backs of polygons
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);
    glEnable(GL_DEPTH_TEST);
        
    cubeTexture = glGenTextures();
    glBindTexture(GL_TEXTURE_CUBE_MAP, cubeTexture);
        
    // Set up texture maps        
    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);       
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
  
    // Load Cube Map images
    for(i <- 0 until 6) {
			// Load this texture map
			val (pBytes, iWidth, iHeight, iComponents, eFormat) = gltReadTGABits(szCubeFaces(i))
			glTexImage2D(cube(i), 0, iComponents, iWidth, iHeight, 0, eFormat, GL_UNSIGNED_BYTE, pBytes);
		}
    glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
    
    viewFrame.MoveForward(-4.0f);
    gltMakeSphere(sphereBatch, 1.0f, 52, 26);
    gltMakeCube(cubeBatch, 20.0f);
    
    reflectionShader = gltLoadShaderPairWithAttributes("Reflection.vp", "Reflection.fp", 2, 
																											 GLT_ATTRIBUTE_VERTEX, "vVertex",
																											 GLT_ATTRIBUTE_NORMAL, "vNormal");
                                                
    locMVPReflect = glGetUniformLocation(reflectionShader, "mvpMatrix");
    locMVReflect = glGetUniformLocation(reflectionShader, "mvMatrix");
    locNormalReflect = glGetUniformLocation(reflectionShader, "normalMatrix");
		locInvertedCamera = glGetUniformLocation(reflectionShader, "mInverseCamera");
                                                
                                                
    skyBoxShader = gltLoadShaderPairWithAttributes("SkyBox.vp", "SkyBox.fp", 2, 
																									 GLT_ATTRIBUTE_VERTEX, "vVertex",
																									 GLT_ATTRIBUTE_NORMAL, "vNormal");

		locMVPSkyBox = glGetUniformLocation(skyBoxShader, "mvpMatrix");

    
	}

	def ShutdownRC() {
    glDeleteTextures(cubeTexture)
	}

	// Called to draw scene
	val mCamera = new M3DMatrix44f
	val mCameraRotOnly = new M3DMatrix44f
	val mInverseCamera = new M3DMatrix44f
	def RenderScene() {
    // Clear the window
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
    
    viewFrame.GetCameraMatrix(mCamera, false);
    viewFrame.GetCameraMatrix(mCameraRotOnly, true);
		m3dInvertMatrix44(mInverseCamera, mCameraRotOnly);

    modelViewMatrix.PushMatrix();    
		// Draw the sphere
		modelViewMatrix.MultMatrix(mCamera);
		glUseProgram(reflectionShader);
		glUniformMatrix4(locMVPReflect, false, transformPipeline.GetModelViewProjectionMatrix());
		glUniformMatrix4(locMVReflect, false, transformPipeline.GetModelViewMatrix());
		glUniformMatrix3(locNormalReflect, false, transformPipeline.GetNormalMatrix());
		glUniformMatrix4(locInvertedCamera, false, mInverseCamera);

		glEnable(GL_CULL_FACE);
		sphereBatch.Draw();
		glDisable(GL_CULL_FACE);
		modelViewMatrix.PopMatrix();

		modelViewMatrix.PushMatrix();
		modelViewMatrix.MultMatrix(mCameraRotOnly);
		glUseProgram(skyBoxShader);
		glUniformMatrix4(locMVPSkyBox, false, transformPipeline.GetModelViewProjectionMatrix());
		cubeBatch.Draw();       
    modelViewMatrix.PopMatrix();
        
    // Do the buffer Swap
    glutSwapBuffers();
	}

	// Respond to arrow keys by moving the camera frame of reference
	def SpecialKeys(key : Int, x : Int, y : Int) {
    if(key == GLUT_KEY_UP)
			viewFrame.MoveForward(0.1f);

    if(key == GLUT_KEY_DOWN)
			viewFrame.MoveForward(-0.1f);

    if(key == GLUT_KEY_LEFT)
			viewFrame.RotateLocalY(0.1f);
      
    if(key == GLUT_KEY_RIGHT)
			viewFrame.RotateLocalY(-0.1f);
                        
    // Refresh the Window
    glutPostRedisplay();
	}

	def ChangeSize(w : Int, _h : Int) {
    // Prevent a divide by zero
    val h = if(_h == 0) 1 else _h
    
    // Set Viewport to window dimensions
    glViewport(0, 0, w, h);
    
    viewFrustum.SetPerspective(35.0f, w.toFloat / h.toFloat, 1.0f, 1000.0f);
    
    projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
    transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);
	}

	def main(args: Array[String]): Unit = {
    glutInit(args);
		
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(800,600);
    glutCreateWindow("OpenGL Cube Maps");
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);
    glutSpecialFunc(SpecialKeys);
    
//    GLenum err = glewInit();
//    if (GLEW_OK != err) {
//			fprintf(stderr, "GLEW Error: %s\n", glewGetErrorString(err));
//			return 1;
//    }
    
    SetupRC();

    glutMainLoop();
    
    ShutdownRC();
        
    return 0;
	}
}
