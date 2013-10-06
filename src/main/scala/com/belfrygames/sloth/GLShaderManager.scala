package com.belfrygames.sloth

import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.Math3D.M3DMatrix44f
import com.belfrygames.sloth.Math3D.M3DVector4f
import com.belfrygames.sloth.Math3D.M3DVector3f
import com.belfrygames.sloth.glut._
import scala.collection.mutable.Map

import java.nio.FloatBuffer

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._

import org.lwjgl.BufferUtils

object GLShaderManager {
  val GLT_SHADER_IDENTITY = 0
  val GLT_SHADER_FLAT = 1
  val GLT_SHADER_SHADED = 2
  val GLT_SHADER_DEFAULT_LIGHT = 3
  val GLT_SHADER_POINT_LIGHT_DIFF = 4
  val GLT_SHADER_TEXTURE_REPLACE = 5
  val GLT_SHADER_TEXTURE_MODULATE = 6
  val GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF = 7
  val GLT_SHADER_TEXTURE_RECT_REPLACE = 8

  val GLT_ATTRIBUTE_VERTEX = 0
  val GLT_ATTRIBUTE_COLOR = 1
  val GLT_ATTRIBUTE_NORMAL = 2
  val GLT_ATTRIBUTE_TEXTURE0 = 3
  val GLT_ATTRIBUTE_TEXTURE1 = 4
  val GLT_ATTRIBUTE_TEXTURE2 = 5
  val GLT_ATTRIBUTE_TEXTURE3 = 6
  val GLT_ATTRIBUTE_LAST = 7

  protected var uiStockShaders = Map.empty[Int, Int]

  // Be warned, going over 128 shaders may cause a hickup for a reallocation.
  def InitializeStockShaders(): Boolean = {
    uiStockShaders(GLT_SHADER_IDENTITY) = gltLoadShaderPairSrcWithAttributes(szIdentityShaderVP, szIdentityShaderFP, 1, GLT_ATTRIBUTE_VERTEX, "vVertex")
    uiStockShaders(GLT_SHADER_FLAT) = gltLoadShaderPairSrcWithAttributes(szFlatShaderVP, szFlatShaderFP, 1, GLT_ATTRIBUTE_VERTEX, "vVertex")
    uiStockShaders(GLT_SHADER_SHADED) = gltLoadShaderPairSrcWithAttributes(szShadedVP, szShadedFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_COLOR, "vColor")

    uiStockShaders(GLT_SHADER_DEFAULT_LIGHT) = gltLoadShaderPairSrcWithAttributes(szDefaultLightVP, szDefaultLightFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_NORMAL, "vNormal")

    uiStockShaders(GLT_SHADER_POINT_LIGHT_DIFF) = gltLoadShaderPairSrcWithAttributes(szPointLightDiffVP, szPointLightDiffFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_NORMAL, "vNormal")

    uiStockShaders(GLT_SHADER_TEXTURE_REPLACE) = gltLoadShaderPairSrcWithAttributes(szTextureReplaceVP, szTextureReplaceFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "vTexCoord0")

    uiStockShaders(GLT_SHADER_TEXTURE_MODULATE) = gltLoadShaderPairSrcWithAttributes(szTextureModulateVP, szTextureModulateFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "vTexCoord0")

    uiStockShaders(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF) = gltLoadShaderPairSrcWithAttributes(szTexturePointLightDiffVP, szTexturePointLightDiffFP, 3, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_NORMAL, "vNormal", GLT_ATTRIBUTE_TEXTURE0, "vTexCoord0")

    uiStockShaders(GLT_SHADER_TEXTURE_RECT_REPLACE) = gltLoadShaderPairSrcWithAttributes(szTextureRectReplaceVP, szTextureRectReplaceFP, 2, GLT_ATTRIBUTE_VERTEX, "vVertex", GLT_ATTRIBUTE_TEXTURE0, "vTexCoord0")

    uiStockShaders(GLT_SHADER_IDENTITY) != 0
  }

  private val FLOAT_SIZE_BYTES = 4
  private val INT_SIZE_BYTES = 4
  private val SHORT_SIZE_BYTES = 2

  def getFloatBuffer(a: Array[Float]): FloatBuffer = {
    val buffer = BufferUtils.createFloatBuffer(a.length)
    buffer.put(a)
    buffer.flip()
    buffer
  }

  private implicit def matrix4fToFloatBuffer(matrix: M3DMatrix44f): FloatBuffer = {
    matrix.array.position(0)
    matrix.array
  }

  private implicit def vector3fToFloatBuffer(vector: M3DVector3f): FloatBuffer = {
    vector.array.position(0)
    vector.array
  }

  private implicit def vector4fToFloatBuffer(vector: M3DVector4f): FloatBuffer = {
    vector.array.position(0)
    vector.array
  }

  def UseStockShader(nShaderID: Int, uniforms: Any*): Int = {
    // Bind to the correct shader
    glUseProgram(uiStockShaders(nShaderID))

    // Set up the uniforms
    var iTransform, iModelMatrix, iProjMatrix, iColor, iLight, iTextureUnit = 0
    var iInteger = 0
    var mvpMatrix: M3DMatrix44f = null
    var pMatrix: M3DMatrix44f = null
    var mvMatrix: M3DMatrix44f = null
    var vColor: M3DVector4f = null
    var vLightPos: M3DVector4f = null

    val va = new VarArgs(uniforms)

    nShaderID match {
      case GLT_SHADER_FLAT => {
        // Just the modelview projection matrix and the color
        iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
        mvpMatrix = va.arg
        glUniformMatrix4(iTransform, false, mvpMatrix)

        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)
      }

      case GLT_SHADER_TEXTURE_RECT_REPLACE | GLT_SHADER_TEXTURE_REPLACE => {
        // Just the texture place
        iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
        mvpMatrix = va.arg
        glUniformMatrix4(iTransform, false, mvpMatrix)

        iTextureUnit = glGetUniformLocation(uiStockShaders(nShaderID), "textureUnit0")
        iInteger = va.arg
        glUniform1i(iTextureUnit, iInteger)
      }

      case GLT_SHADER_TEXTURE_MODULATE => {
        // Multiply the texture by the geometry color
        iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
        mvpMatrix = va.arg
        glUniformMatrix4(iTransform, false, mvpMatrix)

        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)

        iTextureUnit = glGetUniformLocation(uiStockShaders(nShaderID), "textureUnit0")
        iInteger = va.arg
        glUniform1i(iTextureUnit, iInteger)
      }


      case GLT_SHADER_DEFAULT_LIGHT => {
        iModelMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "mvMatrix")
        mvMatrix = va.arg
        glUniformMatrix4(iModelMatrix, false, mvMatrix)

        iProjMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "pMatrix")
        pMatrix = va.arg
        glUniformMatrix4(iProjMatrix, false, pMatrix)

        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)
      }

      case GLT_SHADER_POINT_LIGHT_DIFF => {
        iModelMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "mvMatrix")
        mvMatrix = va.arg
        glUniformMatrix4(iModelMatrix, false, mvMatrix)

        iProjMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "pMatrix")
        pMatrix = va.arg
        glUniformMatrix4(iProjMatrix, false, pMatrix)

        iLight = glGetUniformLocation(uiStockShaders(nShaderID), "vLightPos")
        vLightPos = va.arg
        glUniform3(iLight, vLightPos)

        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)
      }

      case GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF => {
        iModelMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "mvMatrix")
        mvMatrix = va.arg
        glUniformMatrix4(iModelMatrix, false, mvMatrix)

        iProjMatrix = glGetUniformLocation(uiStockShaders(nShaderID), "pMatrix")
        pMatrix = va.arg
        glUniformMatrix4(iProjMatrix, false, pMatrix)

        iLight = glGetUniformLocation(uiStockShaders(nShaderID), "vLightPos")
        vLightPos = va.arg
        glUniform3(iLight, vLightPos)

        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)

        iTextureUnit = glGetUniformLocation(uiStockShaders(nShaderID), "textureUnit0")
        iInteger = va.arg
        glUniform1i(iTextureUnit, iInteger)
      }


      case GLT_SHADER_SHADED => {
        // Just the modelview projection matrix. Color is an attribute
        iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
        pMatrix = va.arg
        glUniformMatrix4(iTransform, false, pMatrix)
      }

      case GLT_SHADER_IDENTITY => {
        // Just the Color
        iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
        vColor = va.arg
        glUniform4(iColor, vColor)
      }
    }

    uiStockShaders(nShaderID)
  }

  private class SHADERLOOKUPETRY(
                                  var szVertexShaderName: String = "",
                                  var szFragShaderName: String = "",
                                  var uiShaderID: Int = 0
                                  )

  // The sourcecode of the book has the code commented and returns 0 so I have no idea what to do :)
  def LookupShader(szVertexProg: String, szFragProg: String = ""): Int = 0

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // Load the shader file, with the supplied named attributes
  def LoadShaderPairWithAttributes(szVertexProgFileName: String, szFragmentProgFileName: String, args: Any*): Int = {
    // Check for duplicate
    val uiShader = LookupShader(szVertexProgFileName, szFragmentProgFileName);
    if (uiShader != 0)
      return uiShader;

    val shaderEntry = new SHADERLOOKUPETRY

    // Temporary Shader objects
    var hVertexShader: Int = 0;
    var hFragmentShader: Int = 0;
    var testVal: Int = 0;

    // Create shader objects
    hVertexShader = glCreateShader(GL_VERTEX_SHADER);
    hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

    // Load them. If fail clean up and return null
    if (!gltLoadShaderFile(szVertexProgFileName, hVertexShader)) {
      glDeleteShader(hVertexShader);
      glDeleteShader(hFragmentShader);
      return 0;
    }

    if (!gltLoadShaderFile(szFragmentProgFileName, hFragmentShader)) {
      glDeleteShader(hVertexShader);
      glDeleteShader(hFragmentShader);
      return 0;
    }

    // Compile them
    glCompileShader(hVertexShader);
    glCompileShader(hFragmentShader);

    // Check for errors
    testVal = glGetShaderi(hVertexShader, GL_COMPILE_STATUS);
    if (testVal == GL_FALSE) {
      glDeleteShader(hVertexShader);
      glDeleteShader(hFragmentShader);
      return 0;
    }

    testVal = glGetShaderi(hFragmentShader, GL_COMPILE_STATUS);
    if (testVal == GL_FALSE) {
      glDeleteShader(hVertexShader);
      glDeleteShader(hFragmentShader);
      return 0;
    }

    // Link them - assuming it works...
    shaderEntry.uiShaderID = glCreateProgram();
    glAttachShader(shaderEntry.uiShaderID, hVertexShader);
    glAttachShader(shaderEntry.uiShaderID, hFragmentShader);


    // List of attributes
    var szNextArg: String = null
    val va = new VarArgs(args)
    val iArgCount: Int = va.arg

    // List of attributes
    for (i <- 0 until iArgCount) {
      val index: Int = va.arg
      szNextArg = va.arg
      glBindAttribLocation(shaderEntry.uiShaderID, index, szNextArg)
    }

    glLinkProgram(shaderEntry.uiShaderID);

    // These are no longer needed
    glDeleteShader(hVertexShader);
    glDeleteShader(hFragmentShader);

    // Make sure link worked too
    testVal = glGetProgrami(shaderEntry.uiShaderID, GL_LINK_STATUS)
    if (testVal == GL_FALSE) {
      glDeleteProgram(shaderEntry.uiShaderID);
      return 0;
    }

    // Add it...
    shaderEntry.szVertexShaderName = szVertexProgFileName
    shaderEntry.szFragShaderName = szFragmentProgFileName
    //	shaderTable.push_back(shaderEntry);
    return shaderEntry.uiShaderID;
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Stock Shader Source Code - Verbatim code for the WIN! ("""...""")
  ///////////////////////////////////////////////////////////////////////////////

  ///////////////////////////////////////////////////////////////////////////////
  // Identity Shader (GLT_SHADER_IDENTITY)
  // This shader does no transformations at all, and uses the current
  // glColor value for fragments.
  // It will shade between verticies.
  val szIdentityShaderVP =
    """attribute vec4 vVertex;
  void main(void) {
	gl_Position = vVertex;
  }"""

  val szIdentityShaderFP =
    """uniform vec4 vColor;
  void main(void) {
	gl_FragColor = vColor;
  }"""

  ///////////////////////////////////////////////////////////////////////////////
  // Flat Shader (GLT_SHADER_FLAT)
  // This shader applies the given model view matrix to the verticies,
  // and uses a uniform color value.
  val szFlatShaderVP =
    """uniform mat4 mvpMatrix;
  attribute vec4 vVertex;
  void main(void) {
	gl_Position = mvpMatrix * vVertex;
  }"""

  val szFlatShaderFP =
    """uniform vec4 vColor;
  void main(void) {
	gl_FragColor = vColor;
  }"""

  ///////////////////////////////////////////////////////////////////////////////
  // GLT_SHADER_SHADED
  // Point light, diffuse lighting only
  val szShadedVP =
    """uniform mat4 mvpMatrix;
  attribute vec4 vColor;
  attribute vec4 vVertex;
  varying vec4 vFragColor;
  void main(void) {
	vFragColor = vColor;
	gl_Position = mvpMatrix * vVertex;
  }"""

  val szShadedFP =
    """varying vec4 vFragColor;
  void main(void) {
	gl_FragColor = vFragColor;
  }"""

  // GLT_SHADER_DEFAULT_LIGHT
  // Simple diffuse, directional, and vertex based light
  val szDefaultLightVP =
    """uniform mat4 mvMatrix;
  uniform mat4 pMatrix;
  varying vec4 vFragColor;
  attribute vec4 vVertex;
  attribute vec3 vNormal;
  uniform vec4 vColor;
  void main(void) {
	mat3 mNormalMatrix;
	mNormalMatrix[0] = mvMatrix[0].xyz;
	mNormalMatrix[1] = mvMatrix[1].xyz;
	mNormalMatrix[2] = mvMatrix[2].xyz;
	vec3 vNorm = normalize(mNormalMatrix * vNormal);
	vec3 vLightDir = vec3(0.0, 0.0, 1.0);
	float fDot = max(0.0, dot(vNorm, vLightDir));
	vFragColor.rgb = vColor.rgb * fDot;
	vFragColor.a = vColor.a;
	mat4 mvpMatrix;
	mvpMatrix = pMatrix * mvMatrix;
	gl_Position = mvpMatrix * vVertex;
  }"""


  val szDefaultLightFP =
    """varying vec4 vFragColor;
  void main(void) {
	gl_FragColor = vFragColor;
  }"""

  //GLT_SHADER_POINT_LIGHT_DIFF
  // Point light, diffuse lighting only
  val szPointLightDiffVP =
    """uniform mat4 mvMatrix;
	  uniform mat4 pMatrix;
	  uniform vec3 vLightPos;
	  uniform vec4 vColor;
	  attribute vec4 vVertex;
	  attribute vec3 vNormal;
	  varying vec4 vFragColor;
	  void main(void) {
		mat3 mNormalMatrix;
		mNormalMatrix[0] = normalize(mvMatrix[0].xyz);
		mNormalMatrix[1] = normalize(mvMatrix[1].xyz);
		mNormalMatrix[2] = normalize(mvMatrix[2].xyz);
		vec3 vNorm = normalize(mNormalMatrix * vNormal);
		vec4 ecPosition;
		vec3 ecPosition3;
		ecPosition = mvMatrix * vVertex;
		ecPosition3 = ecPosition.xyz /ecPosition.w;
		vec3 vLightDir = normalize(vLightPos - ecPosition3);
		float fDot = max(0.0, dot(vNorm, vLightDir));
		vFragColor.rgb = vColor.rgb * fDot;
		vFragColor.a = vColor.a;
		mat4 mvpMatrix;
		mvpMatrix = pMatrix * mvMatrix;
		gl_Position = mvpMatrix * vVertex;
	  }"""

  val szPointLightDiffFP =
    """varying vec4 vFragColor;
  void main(void) {
	gl_FragColor = vFragColor;
  }"""

  //GLT_SHADER_TEXTURE_REPLACE
  // Just put the texture on the polygons
  val szTextureReplaceVP =
    """uniform mat4 mvpMatrix;
  attribute vec4 vVertex;
  attribute vec2 vTexCoord0;
  varying vec2 vTex;
  void main(void) {
	vTex = vTexCoord0;
	gl_Position = mvpMatrix * vVertex;
  }"""

  val szTextureReplaceFP =
    """varying vec2 vTex;
  uniform sampler2D textureUnit0;
  void main(void) {
	gl_FragColor = texture2D(textureUnit0, vTex);
  }"""


  // Just put the texture on the polygons
  val szTextureRectReplaceVP =
    """uniform mat4 mvpMatrix;
  attribute vec4 vVertex;
  attribute vec2 vTexCoord0;
  varying vec2 vTex;
  void main(void) {
	vTex = vTexCoord0;
	gl_Position = mvpMatrix * vVertex;
  }"""

  val szTextureRectReplaceFP =
    """varying vec2 vTex;
  uniform sampler2DRect textureUnit0;
  void main(void) {
	gl_FragColor = texture2DRect(textureUnit0, vTex);
  }"""

  //GLT_SHADER_TEXTURE_MODULATE
  // Just put the texture on the polygons, but multiply by the color (as a unifomr)
  val szTextureModulateVP =
    """uniform mat4 mvpMatrix;
  attribute vec4 vVertex;
  attribute vec2 vTexCoord0;
  varying vec2 vTex;
  void main(void) {
	vTex = vTexCoord0;
	gl_Position = mvpMatrix * vVertex;
  }""";

  val szTextureModulateFP =
    """varying vec2 vTex;
  uniform sampler2D textureUnit0;
  uniform vec4 vColor;
  void main(void) {
	gl_FragColor = vColor * texture2D(textureUnit0, vTex);
  }"""

  //GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF
  // Point light (Diffuse only), with texture (modulated)
  val szTexturePointLightDiffVP =
    """uniform mat4 mvMatrix;
  uniform mat4 pMatrix;
  uniform vec3 vLightPos;
  uniform vec4 vColor;
  attribute vec4 vVertex;
  attribute vec3 vNormal;
  varying vec4 vFragColor;
  attribute vec2 vTexCoord0;
  varying vec2 vTex;
  void main(void) {
	mat3 mNormalMatrix;
	mNormalMatrix[0] = normalize(mvMatrix[0].xyz);
	mNormalMatrix[1] = normalize(mvMatrix[1].xyz);
	mNormalMatrix[2] = normalize(mvMatrix[2].xyz);
	vec3 vNorm = normalize(mNormalMatrix * vNormal);
	vec4 ecPosition;
	vec3 ecPosition3;
	ecPosition = mvMatrix * vVertex;
	ecPosition3 = ecPosition.xyz /ecPosition.w;
	vec3 vLightDir = normalize(vLightPos - ecPosition3);
	float fDot = max(0.0, dot(vNorm, vLightDir));
	vFragColor.rgb = vColor.rgb * fDot;
	vFragColor.a = vColor.a;
	vTex = vTexCoord0;
	mat4 mvpMatrix;
	mvpMatrix = pMatrix * mvMatrix;
	gl_Position = mvpMatrix * vVertex;
  }"""

  val szTexturePointLightDiffFP =
    """varying vec4 vFragColor;
  varying vec2 vTex;
  uniform sampler2D textureUnit0;
  void main(void) {
	gl_FragColor = vFragColor * texture2D(textureUnit0, vTex);
  }"""
}
