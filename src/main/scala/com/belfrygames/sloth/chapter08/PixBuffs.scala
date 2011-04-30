package com.belfrygames.sloth.chapter08

import com.belfrygames.sloth._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.IntArray._
import com.belfrygames.sloth.Math3D.M3DVector._
import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.GLBatch._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import java.nio.ByteBuffer
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL13._
import org.lwjgl.opengl.GL14._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL21._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL31._
import org.lwjgl.opengl.GL32._
import org.lwjgl.opengl.GL33._

object PixBuffs {
	val vGreen = M3DVector( 0.0f, 1.0f, 0.0f, 1.0f )
	val vWhite = M3DVector( 1.0f, 1.0f, 1.0f, 1.0f )
	val vLightPos = M3DVector( 0.0f, 3.0f, 0.0f, 1.0f )

	var screenWidth = 0			// Desired window or desktop width
	var screenHeight = 0			// Desired window or desktop height

	var bFullScreen = false			// Request to run full screen
	var bAnimated = false			// Request for continual updates


	val shaderManager = GLShaderManager			// Shader Manager
	val modelViewMatrix = new GLMatrixStack		// Modelview Matrix
	val projectionMatrix = new GLMatrixStack		// Projection Matrix
	val orthoMatrix = new M3DMatrix44f     
	val viewFrustum = new GLFrustum			// View Frustum
	val transformPipeline = new GLGeometryTransform		// Geometry Transform Pipeline
	val cameraFrame = new GLFrame			// Camera frame

	val torusBatch = new GLTriangleBatch
	val floorBatch = new GLBatch
	val screenQuad = new GLBatch

	val textures = new IntArray(1)
	val blurTextures = new IntArray(6)
	val pixBuffObjs = new IntArray(1)
	
	var curBlurTarget = 0
	var bUsePBOPath = false
	var speedFactor = 0.0f
	var blurProg = 0
	var pixelData : ByteBuffer = null
	var pixelDataSize = 0

	// returns 1 - 6 for blur texture units
	// curPixBuf is always between 0 and 5
	def AdvanceBlurTaget() { curBlurTarget = ((curBlurTarget+ 1) %6); }
	def GetBlurTarget0() : Int = { return (1 + ((curBlurTarget + 5) %6)); }
	def GetBlurTarget1() : Int = { return (1 + ((curBlurTarget + 4) %6)); }
	def GetBlurTarget2() : Int = { return (1 + ((curBlurTarget + 3) %6)); }
	def GetBlurTarget3() : Int = { return (1 + ((curBlurTarget + 2) %6)); }
	def GetBlurTarget4() : Int = { return (1 + ((curBlurTarget + 1) %6)); }
	def GetBlurTarget5() : Int = { return (1 + ((curBlurTarget) %6)); }

	var iFrames = 0;           // Frame count
	lazy val frameTimer = new CStopWatch     // Render time
	def UpdateFrameCount() {
 
    // Reset the stopwatch on first time
    if(iFrames == 0) {
			frameTimer.Reset();
			iFrames += 1;
    }
    // Increment the frame count
    iFrames += 1;

    // Do periodic frame rate calculation
    if (iFrames == 101) {
			val fps = 100.0f / frameTimer.GetElapsedSeconds();
			if (bUsePBOPath)
				printf("Pix_buffs - Using PBOs  %.1f fps\n", fps);
			else
				printf("Pix_buffs - Using Client mem copies %.1f fps\n", fps);

			frameTimer.Reset();
			iFrames = 1;
    }
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////
// Load in a BMP file as a texture. Allows specification of the filters and the wrap mode
	def LoadBMPTexture(szFileName : String, minFilter : Int, magFilter : Int, wrapMode : Int) : Boolean = {
		val (pBits, iWidth, iHeight) = gltReadBMPBits(szFileName);
		if(pBits == null)
			return false;

		// Set Wrap modes
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);

		// Do I need to generate mipmaps?
		if(minFilter == GL_LINEAR_MIPMAP_LINEAR || minFilter == GL_LINEAR_MIPMAP_NEAREST || minFilter == GL_NEAREST_MIPMAP_LINEAR || minFilter == GL_NEAREST_MIPMAP_NEAREST)
			glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, iWidth, iHeight, 0, GL_BGR, GL_UNSIGNED_BYTE, pBits);
		return true;
	}

	///////////////////////////////////////////////////////////////////////////////
	// OpenGL related startup code is safe to put here. Load textures, etc.
	def SetupRC() {
//    GLenum err = glewInit();
//		if (GLEW_OK != err)
//		{
//			/* Problem: glewInit failed, something is seriously wrong. */
//			fprintf(stderr, "Error: %s\n", glewGetErrorString(err));
//		}

		// Initialze Shader Manager
		shaderManager.InitializeStockShaders();
		glEnable(GL_DEPTH_TEST);

		// Black
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		gltMakeTorus(torusBatch, 0.4f, 0.15f, 35, 35);

		val alpha = 0.25f;
		floorBatch.Begin(GL_TRIANGLE_FAN, 4, 1);
		floorBatch.Color4f(0.0f, 1.0f, 0.0f, alpha);
		floorBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
		floorBatch.Normal3f(0.0f, 1.0f, 0.0f);
		floorBatch.Vertex3f(-20.0f, -0.41f, 20.0f);

		floorBatch.Color4f(0.0f, 1.0f, 0.0f, alpha);
		floorBatch.MultiTexCoord2f(0, 10.0f, 0.0f);
		floorBatch.Normal3f(0.0f, 1.0f, 0.0f);
		floorBatch.Vertex3f(20.0f, -0.41f, 20.0f);

		floorBatch.Color4f(0.0f, 1.0f, 0.0f, alpha);
		floorBatch.MultiTexCoord2f(0, 10.0f, 10.0f);
		floorBatch.Normal3f(0.0f, 1.0f, 0.0f);
		floorBatch.Vertex3f(20.0f, -0.41f, -20.0f);

		floorBatch.Color4f(0.0f, 1.0f, 0.0f, alpha);
		floorBatch.MultiTexCoord2f(0, 0.0f, 10.0f);
		floorBatch.Normal3f(0.0f, 1.0f, 0.0f);
		floorBatch.Vertex3f(-20.0f, -0.41f, -20.0f);
		floorBatch.End();
		
		glGenTextures(textures);
		glBindTexture(GL_TEXTURE_2D, textures(0));
		LoadBMPTexture("Marble.bmp", GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR, GL_REPEAT);

		// Create blur program
		blurProg =  gltLoadShaderPairWithAttributes("blur.vs", "blur.fs", 2,
																								GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "texCoord0");

		// Create blur textures
		glGenTextures(blurTextures);

    // XXX I don't think this is necessary. Should set texture data to NULL
		// Allocate a pixel buffer to initialize textures and PBOs
		pixelDataSize = screenWidth * screenHeight * 3 * 4; // XXX This should be unsigned byte
		val data = Buffers.createByteBuffer(pixelDataSize)

		// Setup 6 texture units for blur effect
		// Initialize texture data
		for (i <- 0 until 6) {
			glActiveTexture(GL_TEXTURE1+i);
			glBindTexture(GL_TEXTURE_2D, blurTextures(i));
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenWidth, screenHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, data);
		}

		// Alloc space for copying pixels so we dont call malloc on every draw
		glGenBuffers(pixBuffObjs);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, pixBuffObjs(0));
		glBufferData(GL_PIXEL_PACK_BUFFER, pixelData, GL_DYNAMIC_COPY);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

		// Create geometry and a matrix for screen aligned drawing
		gltGenerateOrtho2DMat(screenWidth, screenHeight, orthoMatrix, screenQuad);

		// Make sure all went well
		gltCheckErrors()
	}


	///////////////////////////////////////////////////////////////////////////////
	// Do your cleanup here. Free textures, display lists, buffer objects, etc.
	def ShutdownRC() {
		// Make sure default FBO is bound
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

		// Cleanup textures
		for (i <- 0 until 7) {
			glActiveTexture(GL_TEXTURE0 + i);
			glBindTexture(GL_TEXTURE_2D, 0);
		}
	
		// Now delete detached textures
		glDeleteTextures(textures);
		glDeleteTextures(blurTextures);

		// delete PBO
		glDeleteBuffers(pixBuffObjs);
	}

	///////////////////////////////////////////////////////////////////////////////
	// This is called at least once and before any rendering occurs. If the screen
	// is a resizeable window, then this will also get called whenever the window
	// is resized.
	def ChangeSize(nWidth : Int, nHeight : Int) {
		glViewport(0, 0, nWidth, nHeight);
		transformPipeline.SetMatrixStacks(modelViewMatrix, projectionMatrix);
		viewFrustum.SetPerspective(35.0f, nWidth.toFloat / nHeight.toFloat, 1.0f, 100.0f);
		projectionMatrix.LoadMatrix(viewFrustum.GetProjectionMatrix());
		modelViewMatrix.LoadIdentity();

		// update screen sizes
		screenWidth = nWidth;
		screenHeight = nHeight;

		// reset screen aligned quad
		gltGenerateOrtho2DMat(screenWidth, screenHeight, orthoMatrix, screenQuad);

		pixelDataSize = screenWidth * screenHeight * 3 * 4; // XXX This should be unsigned byte
		pixelData = Buffers.createByteBuffer(pixelDataSize)

		//  Resize PBOs
		glBindBuffer(GL_PIXEL_PACK_BUFFER, pixBuffObjs(0));
		glBufferData(GL_PIXEL_PACK_BUFFER, pixelData, GL_DYNAMIC_COPY);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

		gltCheckErrors()
	}


	///////////////////////////////////////////////////////////////////////////////
	// Update the camera based on user input, toggle display modes
	lazy val cameraTimer = new CStopWatch
	def ProccessKeys(key : Int, x : Int, y : Int) { 
		val fTime = cameraTimer.GetElapsedSeconds();
		val linear = fTime * 12.0f;
		cameraTimer.Reset(); 

		// Alternate between PBOs and local memory when 'P' is pressed
		if(key == 'P' || key == 'p') 
			bUsePBOPath = !bUsePBOPath

		// Speed up movement
		if(key == '+') {
			speedFactor += linear/2;
			if(speedFactor > 6)
				speedFactor = 6;
		}

		// Slow down moement
		if(key == '-') {
			speedFactor -= linear/2;
			if(speedFactor < 0.5f)
				speedFactor = 0.5f;
		}
	}

	///////////////////////////////////////////////////////////////////////////////
	// Load and setup program for blur effect
	def SetupBlurProg() {
		// Set the blur program as the current one
		glUseProgram(blurProg);

		// Set MVP matrix
		glUniformMatrix4(glGetUniformLocation(blurProg, "mvpMatrix"), false, transformPipeline.GetModelViewProjectionMatrix());

		// Setup the textue units for the blur targets, these rotate every frame
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit0"), GetBlurTarget0());
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit1"), GetBlurTarget1());
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit2"), GetBlurTarget2());
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit3"), GetBlurTarget3());
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit4"), GetBlurTarget4());
		glUniform1i(glGetUniformLocation(blurProg, "textureUnit5"), GetBlurTarget5());
	}

	///////////////////////////////////////////////////////////////////////////////
	// Draw the scene 
	def DrawWorld(yRot : Float, xPos : Float) {
		val mCamera = new M3DMatrix44f
		modelViewMatrix.GetMatrix(mCamera);
	
		// Need light position relative to the Camera
		val vLightTransformed = new M3DVector4f
		m3dTransformVector4(vLightTransformed, vLightPos, mCamera);

		// Draw stuff relative to the camera
		modelViewMatrix.PushMatrix();
		modelViewMatrix.Translate(0.0f, 0.2f, -2.5f);
		modelViewMatrix.Translate(xPos, 0.0f, 0.0f);
		modelViewMatrix.Rotate(yRot, 0.0f, 1.0f, 0.0f);
	
		shaderManager.UseStockShader(GLT_SHADER_POINT_LIGHT_DIFF, 
																 modelViewMatrix.GetMatrix(), 
																 transformPipeline.GetProjectionMatrix(), 
																 vLightTransformed, vGreen, 0);
		torusBatch.Draw();
		modelViewMatrix.PopMatrix();
	}


///////////////////////////////////////////////////////////////////////////////
// Render a frame. The owning framework is responsible for buffer swaps,
// flushes, etc.
	lazy val animationTimer = new CStopWatch
	val totalTime = 6.0f; // To go back and forth
	val halfTotalTime = totalTime / 2.0f;
	def RenderScene() {
		var seconds = animationTimer.GetElapsedSeconds() * speedFactor;
		var xPos = 0.0f;

		// Calculate the next postion of the moving object
		// First perform a mod-like operation on the time as a float
		while(seconds > totalTime)
			seconds -= totalTime;

		// Move object position, if it's gone half way across
		// start bringing it back
		if(seconds < halfTotalTime)
			xPos = seconds -halfTotalTime*0.5f;
		else
			xPos = totalTime - seconds -halfTotalTime*0.5f;

		// First draw world to screen
		modelViewMatrix.PushMatrix();	
		val mCamera = new M3DMatrix44f
		cameraFrame.GetCameraMatrix(mCamera);
		modelViewMatrix.MultMatrix(mCamera);

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textures(0)); // Marble
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		shaderManager.UseStockShader(GLT_SHADER_TEXTURE_MODULATE, transformPipeline.GetModelViewProjectionMatrix(), vWhite, 0);

		floorBatch.Draw();
		DrawWorld(0.0f, xPos);
		modelViewMatrix.PopMatrix();
	
		if(bUsePBOPath) {
			// First bind the PBO as the pack buffer, then read the pixels directly to the PBO
			glBindBuffer(GL_PIXEL_PACK_BUFFER, pixBuffObjs(0));
			glReadPixels(0, 0, screenWidth, screenHeight, GL_RGB, GL_UNSIGNED_BYTE, 0); // LWJGL WARNING!!! If using Pixel Pack Buffer set last arg to 0, otherwise use null
			glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);

			// Next bind the PBO as the unpack buffer, then push the pixels straight into the texture
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pixBuffObjs(0));
        
			// Setup texture unit for new blur, this gets incremented every frame 
			glActiveTexture(GL_TEXTURE0 + GetBlurTarget0()); 
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, screenWidth, screenHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		} else {
			// Grab the screen pixels and copy into local memory
			glReadPixels(0, 0, screenWidth, screenHeight, GL_RGB, GL_UNSIGNED_BYTE, pixelData);
		
			// Push pixels from client memory into texture
			// Setup texture unit for new blur, this gets imcremented every frame
			glActiveTexture(GL_TEXTURE0 + GetBlurTarget0());
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, screenWidth, screenHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, pixelData);
		}

		// Draw full screen quad with blur shader and all blur textures
		projectionMatrix.PushMatrix(); 
		projectionMatrix.LoadIdentity();
		projectionMatrix.LoadMatrix(orthoMatrix);
		modelViewMatrix.PushMatrix();	
		modelViewMatrix.LoadIdentity();
		glDisable(GL_DEPTH_TEST); 
		SetupBlurProg();
		screenQuad.Draw();
		glEnable(GL_DEPTH_TEST); 
		modelViewMatrix.PopMatrix(); 
		projectionMatrix.PopMatrix();

		// Move to the next blur texture for the next frame
		AdvanceBlurTaget();
    
    // Do the buffer Swap
    glutSwapBuffers();
        
    // Do it again
    glutPostRedisplay();

    UpdateFrameCount();
	}


	def main(args: Array[String]): Unit = {
    screenWidth  = 800;
    screenHeight = 600;
    bFullScreen = false; 
    bAnimated   = true;
    bUsePBOPath = false;
    blurProg    = 0;
    speedFactor = 1.0f;

    glutInit(args);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB | GLUT_DEPTH);
    glutInitWindowSize(screenWidth,screenHeight);
  
    glutCreateWindow("Pix Buffs");
 
    glutReshapeFunc(ChangeSize);
    glutDisplayFunc(RenderScene);
    glutKeyboardFunc(ProccessKeys);

    SetupRC();
    glutMainLoop();    
    ShutdownRC();
    return 0;
	}
}
