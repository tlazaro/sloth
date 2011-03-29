package com.belfrygames.sloth

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._

object GLTools {
  def gltSetWorkingDirectory(szArgv : String) {
	// pending
  }
  
  def gltLoadShaderSrc(szShaderSrc : String, shader : Int /*uint*/)	{
	glShaderSource(shader, szShaderSrc)
  }
  
//  def gltLoadShaderPairSrc(szVertexSrc : String, szFragmentSrc : String) : Int = {
//    // Temporary Shader objects
//    var hVertexShader : Int
//    var hFragmentShader : Int
//    val hReturn = 0
//    val testVal : Int
//
//    // Create shader objects
//    hVertexShader = glCreateShader(GL_VERTEX_SHADER);
//    hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
//
//    // Load them.
//    gltLoadShaderSrc(szVertexSrc, hVertexShader);
//    gltLoadShaderSrc(szFragmentSrc, hFragmentShader);
//
//    // Compile them
//    glCompileShader(hVertexShader);
//    glCompileShader(hFragmentShader);
//
//    // Check for errors
//    glGetShaderiv(hVertexShader, GL_COMPILE_STATUS, &testVal);
//    if(testVal == GL_FALSE)
//	{
//	  glDeleteShader(hVertexShader);
//	  glDeleteShader(hFragmentShader);
//	  return (GLuint)NULL;
//	}
//
//    glGetShaderiv(hFragmentShader, GL_COMPILE_STATUS, &testVal);
//    if(testVal == GL_FALSE)
//	{
//	  glDeleteShader(hVertexShader);
//	  glDeleteShader(hFragmentShader);
//	  return (GLuint)NULL;
//	}
//
//    // Link them - assuming it works...
//    hReturn = glCreateProgram();
//    glAttachShader(hReturn, hVertexShader);
//    glAttachShader(hReturn, hFragmentShader);
//    glLinkProgram(hReturn);
//
//    // These are no longer needed
//    glDeleteShader(hVertexShader);
//    glDeleteShader(hFragmentShader);
//
//    // Make sure link worked too
//    glGetProgramiv(hReturn, GL_LINK_STATUS, &testVal);
//    if(testVal == GL_FALSE)
//	{
//	  glDeleteProgram(hReturn);
//	  return (GLuint)NULL;
//	}
//
//    return hReturn;
//  }

/////////////////////////////////////////////////////////////////
// Load a pair of shaders, compile, and link together. Specify the complete
// source code text for each shader. Note, there is no support for
// just loading say a vertex program... you have to do both.
  def gltLoadShaderPairSrcWithAttributes(szVertexSrc : String, szFragmentSrc : String, args : Any*) : Int = {
    // Temporary Shader objects
    var hReturn = 0 //uint

    // Create shader objects
    val hVertexShader = glCreateShader(GL_VERTEX_SHADER)
    val hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER)

    // Load them.
    gltLoadShaderSrc(szVertexSrc, hVertexShader)
    gltLoadShaderSrc(szFragmentSrc, hFragmentShader)

    // Compile them
    glCompileShader(hVertexShader)
    glCompileShader(hFragmentShader)

    // Check for errors
    var testVal = glGetShader(hVertexShader, GL_COMPILE_STATUS)
    if(testVal == GL_FALSE) {
	  glDeleteShader(hVertexShader)
	  glDeleteShader(hFragmentShader)
	  return 0
	}

    testVal = glGetShader(hFragmentShader, GL_COMPILE_STATUS)
    if(testVal == GL_FALSE) {
	  glDeleteShader(hVertexShader)
	  glDeleteShader(hFragmentShader)
	  return 0
	}

    // Link them - assuming it works...
    hReturn = glCreateProgram()
    glAttachShader(hReturn, hVertexShader)
    glAttachShader(hReturn, hFragmentShader)

	var szNextArg : String = null
	val va = new VarArgs(args)
	val iArgCount : Int = va.arg

	// List of attributes
	for (i <- 0 until iArgCount) {
	  val index : Int = va.arg
	  szNextArg = va.arg
	  glBindAttribLocation(hReturn, index, szNextArg)
	}

    glLinkProgram(hReturn)

    // These are no longer needed
    glDeleteShader(hVertexShader)
    glDeleteShader(hFragmentShader)

    // Make sure link worked too
    testVal = glGetProgram(hReturn, GL_LINK_STATUS)
    if(testVal == GL_FALSE) {
	  glDeleteProgram(hReturn);
	  return 0;
	}

    return hReturn;
  }
}
/*
 ///////////////////////////////////////////////////////////////////////////////
 //         THE LIBRARY....
 ///////////////////////////////////////////////////////////////////////////////

 // Get the OpenGL version
 void gltGetOpenGLVersion(GLint &nMajor, GLint &nMinor);

 // Check to see if an exension is supported
 int gltIsExtSupported(const char *szExtension);

 // Set working directoyr to /Resources on the Mac
 void gltSetWorkingDirectory(const char *szArgv);

 ///////////////////////////////////////////////////////////////////////////////
 GLbyte* gltReadBMPBits(const char *szFileName, int *nWidth, int *nHeight);

 /////////////////////////////////////////////////////////////////////////////////////
 // Load a .TGA file
 GLbyte *gltReadTGABits(const char *szFileName, GLint *iWidth, GLint *iHeight, GLint *iComponents, GLenum *eFormat);

 // Capture the frame buffer and write it as a .tga
 // Does not work on the iPhone
 #ifndef OPENGL_ES
 GLint gltGrabScreenTGA(const char *szFileName);
 #endif


 // Make Objects
 void gltMakeTorus(GLTriangleBatch& torusBatch, GLfloat majorRadius, GLfloat minorRadius, GLint numMajor, GLint numMinor);
 void gltMakeSphere(GLTriangleBatch& sphereBatch, GLfloat fRadius, GLint iSlices, GLint iStacks);
 void gltMakeDisk(GLTriangleBatch& diskBatch, GLfloat innerRadius, GLfloat outerRadius, GLint nSlices, GLint nStacks);
 void gltMakeCylinder(GLTriangleBatch& cylinderBatch, GLfloat baseRadius, GLfloat topRadius, GLfloat fLength, GLint numSlices, GLint numStacks);
 void gltMakeCube(GLBatch& cubeBatch, GLfloat fRadius);

 // Shader loading support
 void	gltLoadShaderSrc(const char *szShaderSrc, GLuint shader);
 bool	gltLoadShaderFile(const char *szFile, GLuint shader);

 GLuint	gltLoadShaderPair(const char *szVertexProg, const char *szFragmentProg);
 GLuint   gltLoadShaderPairWithAttributes(const char *szVertexProg, const char *szFragmentProg, ...);

 GLuint gltLoadShaderPairSrc(const char *szVertexSrc, const char *szFragmentSrc);
 GLuint gltLoadShaderPairSrcWithAttributes(const char *szVertexProg, const char *szFragmentProg, ...);

 bool gltCheckErrors(GLuint progName = 0);
 void gltGenerateOrtho2DMat(GLuint width, GLuint height, M3DMatrix44f &orthoMatrix, GLBatch &screenQuad);
 */