/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.belfrygames.sloth

import java.nio.FloatBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._

import org.lwjgl.util.vector.Vector4f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector2f

import com.belfrygames.sloth.GLT_STOCK_SHADER._
import com.belfrygames.sloth.GLT_SHADER_ATTRIBUTE._

class GLBatch extends GLBatchBase {
  override def finalize() {
	// Vertex buffer objects
	if(uiVertexArray != 0)
	  glDeleteBuffers(uiVertexArray)

	if(uiNormalArray != 0)
	  glDeleteBuffers(uiNormalArray)

	if(uiColorArray != 0)
	  glDeleteBuffers(uiColorArray)

	for (array <- uiTextureCoordArray)
	  glDeleteBuffers(array)
  }

  // Start populating the array
  def Begin(primitive : Int, nVerts : Int, nTextureUnits : Int = 0) {
	primitiveType = primitive
	nNumVerts = nVerts

	nNumTextureUnits = if(nTextureUnits > 4) 4 else nTextureUnits

	if(nNumTextureUnits != 0) {
	  // An array of pointers to texture coordinate arrays
	  for(i <- 0 until nNumTextureUnits) {
		uiTextureCoordArray(i) = 0
		pTexCoords(i) = Nil
	  }
	}

	// Vertex Array object for this Array
	vertexArrayObject = glGenVertexArrays()
	glBindVertexArray(vertexArrayObject)
  }

  // Tell the batch you are done
  def End() {
	// Check to see if items have been added one at a time
	if(pVerts != Nil) {
	  glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)
	  glUnmapBuffer(GL_ARRAY_BUFFER)
	  pVerts = Nil
	}

	if(pColors != Nil) {
	  glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)
	  glUnmapBuffer(GL_ARRAY_BUFFER)
	  pColors = Nil
	}

	if(pNormals != Nil) {
	  glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)
	  glUnmapBuffer(GL_ARRAY_BUFFER)
	  pNormals = Nil
	}

	for(i <- 0 until nNumTextureUnits) {
	  if(pTexCoords(i) != Nil) {
		glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(i))
		glUnmapBuffer(GL_ARRAY_BUFFER)
		pTexCoords(i) = Nil
	  }
	}

	// Set up the vertex array object
	glBindVertexArray(vertexArrayObject)

	if(uiVertexArray !=0) {
	  glEnableVertexAttribArray(GLT_ATTRIBUTE_VERTEX)
	  glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)
	  glVertexAttribPointer(GLT_ATTRIBUTE_VERTEX, 3, GL_FLOAT, false, 0, 0)
	}

	if(uiColorArray != 0) {
	  glEnableVertexAttribArray(GLT_ATTRIBUTE_COLOR)
	  glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)
	  glVertexAttribPointer(GLT_ATTRIBUTE_COLOR, 4, GL_FLOAT, false, 0, 0)
	}

	if(uiNormalArray != 0) {
	  glEnableVertexAttribArray(GLT_ATTRIBUTE_NORMAL)
	  glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)
	  glVertexAttribPointer(GLT_ATTRIBUTE_NORMAL, 3, GL_FLOAT, false, 0, 0)
	}

	// How many texture units
	for(i <- 0 until nNumTextureUnits) {
	  if(uiTextureCoordArray(i) != 0) {
		glEnableVertexAttribArray(GLT_ATTRIBUTE_TEXTURE0 + i)
		glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(i))
		glVertexAttribPointer(GLT_ATTRIBUTE_TEXTURE0 + i, 2, GL_FLOAT, false, 0, 0)
	  }
	}

	bBatchDone = true
	glBindVertexArray(0)
  }

  // Public implicit to replace method overloading
  implicit def vector2SeqToFloatSeq(vectors : Seq[Vector2f]) : Seq[Float] = {
	val array = new Array[Float](vectors.size * 2)

	var i = 0
	for (vector <- vectors) {
	  array(i) = vector.x
	  i += 1
	  array(i) = vector.y
	  i += 1
	}

	array
  }
  
  // Public implicit to replace method overloading
  implicit def vector3SeqToFloatSeq(vectors : Seq[Vector3f]) : Seq[Float] = {
	val array = new Array[Float](vectors.size * 3)

	var i = 0
	for (vector <- vectors) {
	  array(i) = vector.x
	  i += 1
	  array(i) = vector.y
	  i += 1
	  array(i) = vector.z
	  i += 1
	}

	array
  }
  
  // Public implicit to replace method overloading
  implicit def vector4SeqToFloatSeq(vectors : Seq[Vector4f]) : Seq[Float] = {
	val array = new Array[Float](vectors.size * 4)

	var i = 0
	for (vector <- vectors) {
	  array(i) = vector.x
	  i += 1
	  array(i) = vector.y
	  i += 1
	  array(i) = vector.z
	  i += 1
	  array(i) = vector.w
	  i += 1
	}

	array
  }

  // Public implicit to replace method overloading
  implicit def vector2ToFloatSeq(vector : Vector2f) : Seq[Float] = Array(vector.x, vector.y)
  // Public implicit to replace method overloading
  implicit def vector3ToFloatSeq(vector : Vector3f) : Seq[Float] = Array(vector.x, vector.y, vector.z)
  // Public implicit to replace method overloading
  implicit def vector4ToFloatSeq(vector : Vector4f) : Seq[Float] = Array(vector.x, vector.y, vector.z, vector.w)

  private implicit def floatSeqfToFloatBuffer(floats : Seq[Float]) : FloatBuffer = {
	val buffer = BufferUtils.createFloatBuffer(floats.length * 4)
	buffer put floats.toArray
	buffer.flip()
	buffer
  }

  def CopyVertexData3f(vVerts : Seq[Float]) {
	// First time, create the buffer object, allocate the space
	if(uiVertexArray == 0) {
	  uiVertexArray = glGenBuffers()
	  glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)
	  glBufferData(GL_ARRAY_BUFFER, vVerts, GL_DYNAMIC_DRAW)
	} else { // Just bind to existing object
	  glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)

	  // Copy the data in
	  glBufferSubData(GL_ARRAY_BUFFER, 0, vVerts)
	  pVerts = Nil
	}
  }

  def CopyNormalDataf(vNorms : Seq[Float]) {
	// First time, create the buffer object, allocate the space
	if(uiNormalArray == 0) {
	  uiNormalArray = glGenBuffers()
	  glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)
	  glBufferData(GL_ARRAY_BUFFER, vNorms, GL_DYNAMIC_DRAW)
	} else {	// Just bind to existing object
	  glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)

	  // Copy the data in
	  glBufferSubData(GL_ARRAY_BUFFER, 0, vNorms)
	  pNormals = Nil
	}
  }
  
  def CopyColorData4f(vColors : Seq[Float]) {
	// First time, create the buffer object, allocate the space
	if(uiColorArray == 0) {
	  uiColorArray = glGenBuffers()
	  glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)
	  glBufferData(GL_ARRAY_BUFFER, vColors, GL_DYNAMIC_DRAW)
	} else {	// Just bind to existing object
	  glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)

	  // Copy the data in
	  glBufferSubData(GL_ARRAY_BUFFER, 0, vColors)
	  pColors = Nil
	}
  }

  def CopyTexCoordData2f(vTexCoords : Seq[Float], uiTextureLayer : Int) {
	// First time, create the buffer object, allocate the space
	if(uiTextureCoordArray(uiTextureLayer) == 0) {
	  uiTextureCoordArray(uiTextureLayer) = glGenBuffers()
	  glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(uiTextureLayer))
	  glBufferData(GL_ARRAY_BUFFER, vTexCoords, GL_DYNAMIC_DRAW)
	}	else {	// Just bind to existing object
	  glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(uiTextureLayer))

	  // Copy the data in
	  glBufferSubData(GL_ARRAY_BUFFER, 0, vTexCoords)
	  pTexCoords(uiTextureLayer) = Nil
	}
  }

  override def Draw() {
	if(!bBatchDone)
	  return

	// Set up the vertex array object
	glBindVertexArray(vertexArrayObject)

	glDrawArrays(primitiveType, 0, nNumVerts)

	glBindVertexArray(0)
  }

  // Just start over. No reallocations, etc.
  def Reset() {
	bBatchDone = false;
	nVertsBuilding = 0;
  }

//  void Vertex3f(GLfloat x, GLfloat y, GLfloat z);
//  void Vertex3fv(M3DVector3f vVertex);
//
//  void Normal3f(GLfloat x, GLfloat y, GLfloat z);
//  void Normal3fv(M3DVector3f vNormal);
//
//  void Color4f(GLfloat r, GLfloat g, GLfloat b, GLfloat a);
//  void Color4fv(M3DVector4f vColor);
//
//  void MultiTexCoord2f(GLuint texture, GLclampf s, GLclampf t);
//  void MultiTexCoord2fv(GLuint texture, M3DVector2f vTexCoord);

  protected var primitiveType = 0		// What am I drawing....

  protected var uiVertexArray : Int = _
  protected var uiNormalArray : Int = _
  protected var uiColorArray : Int = _
//  protected var uiTextureCoordArray : Array[IntBuffer] = Array()
  protected var uiTextureCoordArray : Array[Int] = _
  protected var vertexArrayObject = 0

  protected var nVertsBuilding = 0			// Building up vertexes counter (immediate mode emulator)
  protected var nNumVerts = 0				// Number of verticies in this batch
  protected var nNumTextureUnits = 0		// Number of texture coordinate sets

  protected var	bBatchDone = false;				// Batch has been built

  protected var pVerts : List[Vector3f] = Nil // Array of vertices
  protected var pNormals : List[Vector3f] = Nil // Array of normals
  protected var pColors : List[Vector4f] = Nil // Array of colors
  protected var pTexCoords : Array[List[Vector2f]] = Array(Nil, Nil, Nil, Nil) // Array of texture coordinates
}
