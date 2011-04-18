package com.belfrygames.sloth

import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.Math3D._

import java.io.DataInputStream
import java.io.File
import java.nio.ByteBuffer
import java.util.logging.Level
import java.util.logging.Logger
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL20._
import scala.math._

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

  // Draw a torus (doughnut)  at z = fZVal... torus is in xy plane
  def gltMakeTorus(torusBatch : GLTriangleBatch, majorRadius : Float, minorRadius : Float, numMajor : Int, numMinor : Int) {
    val majorStep = 2.0f*M3D_PI / numMajor;
    val minorStep = 2.0f*M3D_PI / numMinor;

    torusBatch.BeginMesh(numMajor * (numMinor+1) * 6);
    for (i <- 0 until numMajor) {
	  val a0 = i * majorStep;
	  val a1 = a0 + majorStep;
	  val x0 = cos(a0).toFloat
	  val y0 = sin(a0).toFloat
	  val x1 = cos(a1).toFloat
	  val y1 = sin(a1).toFloat

	  val vVertex = new M3DVector3fArray(4)
	  val vNormal = new M3DVector3fArray(4)
	  val vTexture = new M3DVector2fArray(4)

	  for (j <- 0 until numMinor) {
		var b = j * minorStep;
		var c = cos(b).toFloat
		var r = minorRadius * c + majorRadius;
		var z = minorRadius * sin(b).toFloat

		// First point
		vTexture(0)(0) = (i) / numMajor.toFloat
		vTexture(0)(1) = (j) / numMinor.toFloat
		vNormal(0)(0) = x0*c;
		vNormal(0)(1) = y0*c;
		vNormal(0)(2) = z/minorRadius;
		m3dNormalizeVector3(vNormal(0));
		vVertex(0)(0) = x0 * r;
		vVertex(0)(1) = y0 * r;
		vVertex(0)(2) = z;

		// Second point
		vTexture(1)(0) = (i+1) / numMajor.toFloat
		vTexture(1)(1) = (j) / numMinor.toFloat
		vNormal(1)(0) = x1*c;
		vNormal(1)(1) = y1*c;
		vNormal(1)(2) = z / minorRadius;
		m3dNormalizeVector3(vNormal(1));
		vVertex(1)(0) = x1*r;
		vVertex(1)(1) = y1*r;
		vVertex(1)(2) = z;

		// Next one over
		b = (j+1) * minorStep;
		c = cos(b).toFloat
		r = minorRadius * c + majorRadius;
		z = minorRadius * sin(b).toFloat

		// Third (based on first)
		vTexture(2)(0) = (i) / numMajor.toFloat
		vTexture(2)(1) = (j+1)/numMinor.toFloat
		vNormal(2)(0) = x0*c;
		vNormal(2)(1) = y0*c;
		vNormal(2)(2) = z/minorRadius;
		m3dNormalizeVector3(vNormal(2));
		vVertex(2)(0) = x0 * r;
		vVertex(2)(1) = y0 * r;
		vVertex(2)(2) = z;

		// Fourth (based on second)
		vTexture(3)(0) = (i+1) / numMajor.toFloat
		vTexture(3)(1) = (j+1) / numMinor.toFloat
		vNormal(3)(0) = x1 * c;
		vNormal(3)(1) = y1 * c;
		vNormal(3)(2) = z / minorRadius;
		m3dNormalizeVector3(vNormal(3));
		vVertex(3)(0) = x1*r;
		vVertex(3)(1) = y1*r;
		vVertex(3)(2) = z;

		torusBatch.AddTriangle(vVertex, vNormal, vTexture);

		// Rearrange for next triangle
		vVertex(0).copy(vVertex(1))
		vNormal(0).copy(vNormal(1))
		vTexture(0).copy(vTexture(1))

		vVertex(1).copy(vVertex(3))
		vNormal(1).copy(vNormal(3))
		vTexture(1).copy(vTexture(3))

		torusBatch.AddTriangle(vVertex, vNormal, vTexture);
	  }
	}
	torusBatch.End();
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  // Make a sphere
  def gltMakeSphere(sphereBatch : GLTriangleBatch, fRadius : Float, iSlices : Int, iStacks : Int) {
    val drho = (3.141592653589.toFloat) / iStacks.toFloat
    val dtheta = 2.0f * (3.141592653589.toFloat) / iSlices.toFloat
	val ds = 1.0f / iSlices.toFloat
	val dt = 1.0f / iStacks.toFloat
	var t = 1.0f;
	var s = 0.0f;

    sphereBatch.BeginMesh(iSlices * iStacks * 6);
	for (i <- 0 until iStacks) {
	  val rho = i * drho;
	  val srho = sin(rho).toFloat
	  val crho = (cos(rho)).toFloat
	  val srhodrho = sin(rho + drho).toFloat
	  val crhodrho = cos(rho + drho).toFloat

	  // Many sources of OpenGL sphere drawing code uses a triangle fan
	  // for the caps of the sphere. This however introduces texturing
	  // artifacts at the poles on some OpenGL implementations
	  s = 0.0f;
	  val vVertex = new M3DVector3fArray(4)
	  val vNormal = new M3DVector3fArray(4)
	  val vTexture = new M3DVector2fArray(4)

	  for (j <- 0 until iSlices) {
		var theta = if(j == iSlices) { 0.0f } else { j * dtheta }
		var stheta = (-sin(theta)).toFloat
		var ctheta = (cos(theta)).toFloat

		var x = stheta * srho;
		var y = ctheta * srho;
		var z = crho;

		vTexture(0)(0) = s;
		vTexture(0)(1) = t;
		vNormal(0)(0) = x;
		vNormal(0)(1) = y;
		vNormal(0)(2) = z;
		vVertex(0)(0) = x * fRadius;
		vVertex(0)(1) = y * fRadius;
		vVertex(0)(2) = z * fRadius;

		x = stheta * srhodrho;
		y = ctheta * srhodrho;
		z = crhodrho;

		vTexture(1)(0) = s;
		vTexture(1)(1) = t - dt;
		vNormal(1)(0) = x;
		vNormal(1)(1) = y;
		vNormal(1)(2) = z;
		vVertex(1)(0) = x * fRadius;
		vVertex(1)(1) = y * fRadius;
		vVertex(1)(2) = z * fRadius;


		theta = if ((j+1) == iSlices) { 0.0f } else { (j+1) * dtheta }
		stheta = (-sin(theta)).toFloat
		ctheta = cos(theta).toFloat

		x = stheta * srho;
		y = ctheta * srho;
		z = crho;

		s += ds;
		vTexture(2)(0) = s;
		vTexture(2)(1) = t;
		vNormal(2)(0) = x;
		vNormal(2)(1) = y;
		vNormal(2)(2) = z;
		vVertex(2)(0) = x * fRadius;
		vVertex(2)(1) = y * fRadius;
		vVertex(2)(2) = z * fRadius;

		x = stheta * srhodrho;
		y = ctheta * srhodrho;
		z = crhodrho;

		vTexture(3)(0) = s;
		vTexture(3)(1) = t - dt;
		vNormal(3)(0) = x;
		vNormal(3)(1) = y;
		vNormal(3)(2) = z;
		vVertex(3)(0) = x * fRadius;
		vVertex(3)(1) = y * fRadius;
		vVertex(3)(2) = z * fRadius;

		sphereBatch.AddTriangle(vVertex, vNormal, vTexture);

		// Rearrange for next triangle
		vVertex(0).copy(vVertex(1))
		vNormal(0).copy(vNormal(1))
		vTexture(0).copy(vTexture(1))

		vVertex(1).copy(vVertex(3))
		vNormal(1).copy(vNormal(3))
		vTexture(1).copy(vTexture(3))

		sphereBatch.AddTriangle(vVertex, vNormal, vTexture);
	  }
	  t -= dt;
	}
	sphereBatch.End();
  }

  ////////////////////////////////////////////////////////////////////////////////////////
  def gltMakeDisk(diskBatch : GLTriangleBatch, innerRadius : Float, outerRadius : Float, nSlices : Int, nStacks : Int) {
	// How much to step out each stack
	var fStepSizeRadial = outerRadius - innerRadius;
	if(fStepSizeRadial < 0.0f)			// Dum dum...
	  fStepSizeRadial *= -1.0f;

	fStepSizeRadial /= nStacks.toFloat

	val fStepSizeSlice = (3.1415926536f * 2.0f) / nSlices.toFloat

	diskBatch.BeginMesh(nSlices * nStacks * 6);

	val vVertex = new M3DVector3fArray(4)
	val vNormal = new M3DVector3fArray(4)
	val vTexture = new M3DVector2fArray(4)

	val fRadialScale = 1.0f / outerRadius;

	for(i <- 0 until nStacks) {
	  for(j <- 0 until nSlices) {
		val inner = innerRadius + (i.toFloat) * fStepSizeRadial;
		val outer = innerRadius + ((i+1).toFloat) * fStepSizeRadial;

		var theyta = fStepSizeSlice * j.toFloat;
		var theytaNext = if(j == (nSlices - 1))
		  0.0f;
		else
		  fStepSizeSlice * (j+1).toFloat;

		// Inner First
		vVertex(0)(0) = cos(theyta).toFloat * inner;	// X
		vVertex(0)(1) = sin(theyta).toFloat * inner;	// Y
		vVertex(0)(2) = 0.0f;					// Z

		vNormal(0)(0) = 0.0f;					// Surface Normal, same for everybody
		vNormal(0)(1) = 0.0f;
		vNormal(0)(2) = 1.0f;

		vTexture(0)(0) = ((vVertex(0)(0) * fRadialScale) + 1.0f) * 0.5f;
		vTexture(0)(1) = ((vVertex(0)(1) * fRadialScale) + 1.0f) * 0.5f;

		// Outer First
		vVertex(1)(0) = cos(theyta).toFloat * outer;	// X
		vVertex(1)(1) = sin(theyta).toFloat * outer;	// Y
		vVertex(1)(2) = 0.0f;					// Z

		vNormal(1)(0) = 0.0f;					// Surface Normal, same for everybody
		vNormal(1)(1) = 0.0f;
		vNormal(1)(2) = 1.0f;

		vTexture(1)(0) = ((vVertex(1)(0) * fRadialScale) + 1.0f) * 0.5f;
		vTexture(1)(1) = ((vVertex(1)(1) * fRadialScale) + 1.0f) * 0.5f;

		// Inner Second
		vVertex(2)(0) = cos(theytaNext).toFloat * inner;	// X
		vVertex(2)(1) = sin(theytaNext).toFloat * inner;	// Y
		vVertex(2)(2) = 0.0f;					// Z

		vNormal(2)(0) = 0.0f;					// Surface Normal, same for everybody
		vNormal(2)(1) = 0.0f;
		vNormal(2)(2) = 1.0f;

		vTexture(2)(0) = ((vVertex(2)(0) * fRadialScale) + 1.0f) * 0.5f;
		vTexture(2)(1) = ((vVertex(2)(1) * fRadialScale) + 1.0f) * 0.5f;


		// Outer Second
		vVertex(3)(0) = cos(theytaNext).toFloat * outer;	// X
		vVertex(3)(1) = sin(theytaNext).toFloat * outer;	// Y
		vVertex(3)(2) = 0.0f;					// Z

		vNormal(3)(0) = 0.0f;					// Surface Normal, same for everybody
		vNormal(3)(1) = 0.0f;
		vNormal(3)(2) = 1.0f;

		vTexture(3)(0) = ((vVertex(3)(0) * fRadialScale) + 1.0f) * 0.5f;
		vTexture(3)(1) = ((vVertex(3)(1) * fRadialScale) + 1.0f) * 0.5f;

		diskBatch.AddTriangle(vVertex, vNormal, vTexture);

		// Rearrange for next triangle
		vVertex(0).copy(vVertex(1))
		vNormal(0).copy(vNormal(1))
		vTexture(0).copy(vTexture(1))

		vVertex(1).copy(vVertex(3))
		vNormal(1).copy(vNormal(3))
		vTexture(1).copy(vTexture(3))

		diskBatch.AddTriangle(vVertex, vNormal, vTexture);
	  }
	}

	diskBatch.End();
  }

// Draw a cylinder. Much like gluCylinder
  def gltMakeCylinder(cylinderBatch : GLTriangleBatch, baseRadius : Float, topRadius : Float, fLength : Float, numSlices : Int, numStacks : Int) {
    val fRadiusStep = (topRadius - baseRadius) / numStacks.toFloat;

	val fStepSizeSlice = (3.1415926536f * 2.0f) / numSlices.toFloat;

	val vVertex = new M3DVector3fArray(4)
	val vNormal = new M3DVector3fArray(4)
	val vTexture = new M3DVector2fArray(4)

    cylinderBatch.BeginMesh(numSlices * numStacks * 6);

    val ds = 1.0f / numSlices;
	val dt = 1.0f / numStacks;

	for (i <- 0 until numStacks)
	{
	  val t = if(i == 0)
		0.0f;
	  else
		i * dt;

	  val tNext = if(i == (numStacks - 1))
		1.0f;
	  else
		(i+1) * dt;

	  val fCurrentRadius = baseRadius + (fRadiusStep * (i));
	  val fNextRadius = baseRadius + (fRadiusStep * (i+1));

	  val fCurrentZ = (i) * (fLength / numStacks.toFloat);
	  val fNextZ = (i+1) * (fLength / numStacks.toFloat);

	  var zNormal = 0.0f;
	  if(!m3dCloseEnough(baseRadius - topRadius, 0.0f, 0.00001f)) {
		// Rise over run...
		zNormal = (baseRadius - topRadius);
	  }

	  for (j <- 0 until numSlices)
	  {
		val s = if(j == 0)
		  0.0f;
		else
		  j * ds;

		val sNext = if(j == (numSlices -1))
		  1.0f;
		else
		  (j+1) * ds;

		val theyta = fStepSizeSlice * j;
		val theytaNext = if(j == (numSlices - 1))
		  0.0f;
		else
		  fStepSizeSlice * (j+1);

		// Inner First
		vVertex(1)(0) = cos(theyta).toFloat * fCurrentRadius;	// X
		vVertex(1)(1) = sin(theyta).toFloat * fCurrentRadius;	// Y
		vVertex(1)(2) = fCurrentZ;						// Z

		vNormal(1)(0) = vVertex(1)(0);					// Surface Normal, same for everybody
		vNormal(1)(1) = vVertex(1)(1);
		vNormal(1)(2) = zNormal;
		m3dNormalizeVector3(vNormal(1));

		vTexture(1)(0) = s;					// Texture Coordinates, I have no idea...
		vTexture(1)(1) = t;

		// Outer First
		vVertex(0)(0) = cos(theyta).toFloat * fNextRadius;	// X
		vVertex(0)(1) = sin(theyta).toFloat * fNextRadius;	// Y
		vVertex(0)(2) = fNextZ;						// Z

		if(!m3dCloseEnough(fNextRadius, 0.0f, 0.00001f)) {
		  vNormal(0)(0) = vVertex(0)(0);					// Surface Normal, same for everybody
		  vNormal(0)(1) = vVertex(0)(1);					// For cones, tip is tricky
		  vNormal(0)(2) = zNormal;
		  m3dNormalizeVector3(vNormal(0));
		}
		else
		  vNormal(0).copy(vNormal(1))


		vTexture(0)(0) = s;					// Texture Coordinates, I have no idea...
		vTexture(0)(1) = tNext;

		// Inner second
		vVertex(3)(0) = cos(theytaNext).toFloat * fCurrentRadius;	// X
		vVertex(3)(1) = sin(theytaNext).toFloat * fCurrentRadius;	// Y
		vVertex(3)(2) = fCurrentZ;						// Z

		vNormal(3)(0) = vVertex(3)(0);					// Surface Normal, same for everybody
		vNormal(3)(1) = vVertex(3)(1);
		vNormal(3)(2) = zNormal;
		m3dNormalizeVector3(vNormal(3));

		vTexture(3)(0) = sNext;					// Texture Coordinates, I have no idea...
		vTexture(3)(1) = t;

		// Outer second
		vVertex(2)(0) = cos(theytaNext).toFloat * fNextRadius;	// X
		vVertex(2)(1) = sin(theytaNext).toFloat * fNextRadius;	// Y
		vVertex(2)(2) = fNextZ;						// Z

		if(!m3dCloseEnough(fNextRadius, 0.0f, 0.00001f)) {
		  vNormal(2)(0) = vVertex(2)(0);					// Surface Normal, same for everybody
		  vNormal(2)(1) = vVertex(2)(1);
		  vNormal(2)(2) = zNormal;
		  m3dNormalizeVector3(vNormal(2));
		}
		else
		  vNormal(2).copy(vNormal(3))


		vTexture(2)(0) = sNext;					// Texture Coordinates, I have no idea...
		vTexture(2)(1) = tNext;

		cylinderBatch.AddTriangle(vVertex, vNormal, vTexture);

		// Rearrange for next triangle
		vVertex(0).copy(vVertex(1))
		vNormal(0).copy(vNormal(1))
		vTexture(0).copy(vTexture(1))

		vVertex(1).copy(vVertex(3))
		vNormal(1).copy(vNormal(3))
		vTexture(1).copy(vTexture(3))

		cylinderBatch.AddTriangle(vVertex, vNormal, vTexture);
	  }
	}
	cylinderBatch.End();
  }


  ///////////////////////////////////////////////////////////////////////////////////////
  // Make a cube, centered at the origin, and with a specified "radius"
  def gltMakeCube(cubeBatch : GLBatch, fRadius : Float) {
    cubeBatch.Begin(GL_TRIANGLES, 36, 1);

    /////////////////////////////////////////////
    // Top of cube
    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, fRadius);


    ////////////////////////////////////////////
    // Bottom of cube
    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, -fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, -fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, -fRadius, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, -fRadius, fRadius);

    ///////////////////////////////////////////
    // Left side of cube
    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(-fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(-fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, -fRadius, fRadius);

    // Right side of cube
    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(fRadius, -fRadius, fRadius);

    cubeBatch.Normal3f(fRadius, 0.0f, 0.0f);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, -fRadius);

    // Front and Back
    // Front
    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, fRadius);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, fRadius);

    // Back
    cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
    cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
    cubeBatch.Vertex3f(fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, 0.0f);
    cubeBatch.Vertex3f(-fRadius, -fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
    cubeBatch.MultiTexCoord2f(0, 0.0f, fRadius);
    cubeBatch.Vertex3f(-fRadius, fRadius, -fRadius);

    cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
    cubeBatch.MultiTexCoord2f(0, fRadius, fRadius);
    cubeBatch.Vertex3f(fRadius, fRadius, -fRadius);

	cubeBatch.Normal3f(0.0f, 0.0f, -fRadius);
	cubeBatch.MultiTexCoord2f(0, fRadius, 0.0f);
	cubeBatch.Vertex3f(fRadius, -fRadius, -fRadius);
    cubeBatch.End();
  }

  // Define targa header. This is only used locally.
  private class TGAHEADER (
	var identsize : Byte,              // Size of ID field that follows header (0)
	var colorMapType : Byte,           // 0 = None, 1 = paletted
	var imageType : Byte,              // 0 = none, 1 = indexed, 2 = rgb, 3 = grey, +8=rle
	var colorMapStart : Short,          // First colour map entry
	var colorMapLength : Short,         // Number of colors
	var colorMapBits : Byte,   // bits per palette entry
	var xstart : Short,                 // image x origin
	var ystart : Short,                 // image y origin
	var width : Short,                  // width in pixels
	var height : Short,                 // height in pixels
	var bits : Byte,                   // bits per pixel (8 16, 24, 32)
	var descriptor : Byte             // image descriptor
  )

  ////////////////////////////////////////////////////////////////////
// Allocate memory and load targa bits. Returns pointer to new buffer,
// height, and width of texture, and the OpenGL format of data.
// Call free() on buffer when finished!
// This only works on pretty vanilla targas... 8, 24, or 32 bit color
// only, no palettes, no RLE encoding.
  def gltReadTGABits(szFileName : String) : Tuple5[ByteBuffer, Int, Int, Int, Int] = {
    // Default/Failed values
    var eFormat = GL_RGB;
    var iComponents = GL_RGB;
	var iWidth = 0
	var iHeight = 0
	var pBits : ByteBuffer = null

    // Attempt to open the file
	var input : LEDataInputStream = null

	try {
	  input = new LEDataInputStream(getClass().getClassLoader().getResourceAsStream("com/belfrygames/sloth/resources/" + szFileName))

	  //Read in header (binary)
	  val tgaHeader = new TGAHEADER(input.readByte,
									input.readByte,
									input.readByte,
									input.readShort,
									input.readShort,
									input.readByte,
									input.readShort,
									input.readShort,
									input.readShort,
									input.readShort,
									input.readByte,
									input.readByte)

	  // Get width, height, and depth of texture
	  iWidth = tgaHeader.width;
	  iHeight = tgaHeader.height;
	  val sDepth = tgaHeader.bits / 8;

	  // Put some validity checks here. Very simply, I only understand
	  // or care about 8, 24, or 32 bit targa's.
	  if(tgaHeader.bits != 8 && tgaHeader.bits != 24 && tgaHeader.bits != 32)
		throw new Exception("Wrong bits in TGA: " + tgaHeader.bits)

	  // Calculate size of image buffer
	  val lImageSize = tgaHeader.width * tgaHeader.height * sDepth;

	  // Allocate memory and check for success
	  pBits = Buffers.createByteBuffer(lImageSize)

	  // Read in the bits
	  // Check for read error. This should catch RLE or other weird formats that I don't want to recognize
	  if (pBits.hasArray) {
		input.readFully(pBits.array)
	  } else {
		var bytes = new Array[Byte](lImageSize)
		input.readFully(bytes)
		pBits.put(bytes)
		pBits.flip
	  }

	  // Set OpenGL format expected
	  sDepth match {
		case 3 => {
			eFormat = GL_BGR;
			iComponents = GL_RGB;
		  }
		case 4 => {
			eFormat = GL_BGRA;
			iComponents = GL_RGBA;
		  }
		case 1 => {
			eFormat = GL_LUMINANCE;
			iComponents = GL_LUMINANCE;
		  }
	  }
	} catch {
	  case ex : Exception => {
		  pBits = null
		  Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
	  }
	} finally {
	  try {
		input.close
	  } catch {
		case ex : Exception => Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
	  }
	}

    // Return pointer to image data
    return (pBits, iWidth, iHeight, iComponents, eFormat);
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