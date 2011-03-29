package com.belfrygames.sloth

import GLTools._
import scala.collection.mutable.Map

import java.nio.FloatBuffer

import org.lwjgl.opengl.GL20._

import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector4f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.BufferUtils

object GLT_STOCK_SHADER extends Enumeration {
  val GLT_SHADER_IDENTITY, GLT_SHADER_FLAT, GLT_SHADER_SHADED, GLT_SHADER_DEFAULT_LIGHT, GLT_SHADER_POINT_LIGHT_DIFF, GLT_SHADER_TEXTURE_REPLACE,
  GLT_SHADER_TEXTURE_MODULATE, GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF, GLT_SHADER_TEXTURE_RECT_REPLACE = Value
}

object GLT_SHADER_ATTRIBUTE extends Enumeration {
  val GLT_ATTRIBUTE_VERTEX, GLT_ATTRIBUTE_COLOR, GLT_ATTRIBUTE_NORMAL, GLT_ATTRIBUTE_TEXTURE0, GLT_ATTRIBUTE_TEXTURE1, GLT_ATTRIBUTE_TEXTURE2,
  GLT_ATTRIBUTE_TEXTURE3, GLT_ATTRIBUTE_LAST = Value

  // Allow usage as Int in LWJGL methods
  implicit def GLT_SHADER_ATTRIBUTE2Int (attrib : GLT_SHADER_ATTRIBUTE.Value) : Int = attrib.id
}

object GLShaderManager {

  import GLT_STOCK_SHADER._
  import GLT_SHADER_ATTRIBUTE._

  case class SHADERLOOKUPETRY (szVertexShaderName : String, szFragShaderName : String, uiShaderID : Int)

  protected var uiStockShaders = Map.empty[GLT_STOCK_SHADER.Value, Int]

  // Be warned, going over 128 shaders may cause a hickup for a reallocation.
  // Notice the .id !!!
  def InitializeStockShaders() : Boolean = {
	uiStockShaders(GLT_SHADER_IDENTITY) = gltLoadShaderPairSrcWithAttributes(szIdentityShaderVP, szIdentityShaderFP, 1, GLT_ATTRIBUTE_VERTEX.id, "vVertex")
	uiStockShaders(GLT_SHADER_FLAT) = gltLoadShaderPairSrcWithAttributes(szFlatShaderVP, szFlatShaderFP, 1, GLT_ATTRIBUTE_VERTEX.id, "vVertex")
	uiStockShaders(GLT_SHADER_SHADED) = gltLoadShaderPairSrcWithAttributes(szShadedVP, szShadedFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_COLOR.id, "vColor")

	uiStockShaders(GLT_SHADER_DEFAULT_LIGHT) = gltLoadShaderPairSrcWithAttributes(szDefaultLightVP, szDefaultLightFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_NORMAL.id, "vNormal")

	uiStockShaders(GLT_SHADER_POINT_LIGHT_DIFF) = gltLoadShaderPairSrcWithAttributes(szPointLightDiffVP, szPointLightDiffFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_NORMAL.id, "vNormal")

	uiStockShaders(GLT_SHADER_TEXTURE_REPLACE) = gltLoadShaderPairSrcWithAttributes(szTextureReplaceVP, szTextureReplaceFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_TEXTURE0.id, "vTexCoord0")

	uiStockShaders(GLT_SHADER_TEXTURE_MODULATE) = gltLoadShaderPairSrcWithAttributes(szTextureModulateVP, szTextureModulateFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_TEXTURE0.id, "vTexCoord0")

	uiStockShaders(GLT_SHADER_TEXTURE_POINT_LIGHT_DIFF) = gltLoadShaderPairSrcWithAttributes(szTexturePointLightDiffVP, szTexturePointLightDiffFP, 3, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_NORMAL.id, "vNormal", GLT_ATTRIBUTE_TEXTURE0.id, "vTexCoord0")

    uiStockShaders(GLT_SHADER_TEXTURE_RECT_REPLACE) = gltLoadShaderPairSrcWithAttributes(szTextureRectReplaceVP, szTextureRectReplaceFP, 2, GLT_ATTRIBUTE_VERTEX.id, "vVertex", GLT_ATTRIBUTE_TEXTURE0.id, "vTexCoord0")

	uiStockShaders(GLT_SHADER_IDENTITY) != 0
  }

  private val FLOAT_SIZE_BYTES = 4
  private val INT_SIZE_BYTES = 4
  private val SHORT_SIZE_BYTES = 2

  def getFloatBuffer(a : Array[Float]) : FloatBuffer = {
	val buffer = BufferUtils.createFloatBuffer(a.length * FLOAT_SIZE_BYTES)
	buffer.put(a)
	buffer.flip()
	buffer
  }

  private implicit def matrix4fToFloatBuffer(matrix : Matrix4f) : FloatBuffer = {
	getFloatBuffer(Array(matrix.m00, matrix.m01, matrix.m02, matrix.m03,
						 matrix.m10, matrix.m11, matrix.m12, matrix.m13,
						 matrix.m20, matrix.m21, matrix.m22, matrix.m23,
						 matrix.m30, matrix.m31, matrix.m32, matrix.m33))
  }

  private implicit def vector3fToFloatBuffer(vector : Vector3f) : FloatBuffer = {
	getFloatBuffer(Array(vector.x, vector.y, vector.z))
  }

  private implicit def vector4fToFloatBuffer(vector : Vector4f) : FloatBuffer = {
	getFloatBuffer(Array(vector.x, vector.y, vector.z, vector.w))
  }

  def UseStockShader(nShaderID : GLT_STOCK_SHADER.Value, uniforms : Any*) : Int = {
	// Bind to the correct shader
	glUseProgram(uiStockShaders(nShaderID))

	// Set up the uniforms
	var iTransform, iModelMatrix, iProjMatrix, iColor, iLight, iTextureUnit = 0
	var iInteger = 0
	var mvpMatrix : Matrix4f = null
	var pMatrix : Matrix4f = null
	var mvMatrix : Matrix4f = null
	var vColor : Vector4f = null
	var vLightPos : Vector3f = null

	val va = new VarArgs(uniforms)

	nShaderID match {
	  case GLT_SHADER_FLAT => {			// Just the modelview projection matrix and the color
		  iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
		  mvpMatrix = va.arg
		  glUniformMatrix4(iTransform, false, mvpMatrix)

		  iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
		  vColor = va.arg
		  glUniform4(iColor, vColor)
		}

	  case GLT_SHADER_TEXTURE_RECT_REPLACE | GLT_SHADER_TEXTURE_REPLACE => { // Just the texture place
		  iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
		  mvpMatrix = va.arg
		  glUniformMatrix4(iTransform, false, mvpMatrix)

		  iTextureUnit = glGetUniformLocation(uiStockShaders(nShaderID), "textureUnit0")
		  iInteger = va.arg
		  glUniform1i(iTextureUnit, iInteger)
		}

	  case GLT_SHADER_TEXTURE_MODULATE => { // Multiply the texture by the geometry color
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


	  case GLT_SHADER_SHADED =>	{	// Just the modelview projection matrix. Color is an attribute
		  iTransform = glGetUniformLocation(uiStockShaders(nShaderID), "mvpMatrix")
		  pMatrix = va.arg
		  glUniformMatrix4(iTransform, false, pMatrix)
		}

	  case GLT_SHADER_IDENTITY =>	{// Just the Color
		  iColor = glGetUniformLocation(uiStockShaders(nShaderID), "vColor")
		  vColor = va.arg
		  glUniform4(iColor, vColor)
		}
	}

	uiStockShaders(nShaderID)
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
