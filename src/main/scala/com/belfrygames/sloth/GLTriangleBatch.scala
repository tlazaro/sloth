package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D._
import java.nio.FloatBuffer
import java.nio.IntBuffer
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._
import org.lwjgl.BufferUtils
import GLT_SHADER_ATTRIBUTE._

class GLTriangleBatch extends GLBatchBase {
  val VERTEX_DATA =    0
  val NORMAL_DATA =    1
  val TEXTURE_DATA =   2
  val INDEX_DATA =     3

  override def finalize() {
	// Delete buffer objects
    glDeleteBuffers(bufferObjects)

	glDeleteVertexArrays(vertexArrayBufferObject)
  }
  // Use these three functions to add triangles
  def BeginMesh(nMaxVerts : Int) {
	nMaxIndexes = nMaxVerts
    nNumIndexes = 0
    nNumVerts = 0

    // Allocate new blocks. In reality, the other arrays will be much shorter than the index array
    pIndexes = new Array(nMaxIndexes)
    pVerts = M3DVector.array(nMaxIndexes)
    pNorms = M3DVector.array(nMaxIndexes)
    pTexCoords = M3DVector.array(nMaxIndexes)
  }

  def AddTriangle(verts : Array[M3DVector3f], vNorms : Array[M3DVector3f], vTexCoords : Array[M3DVector2f]) {
	val e = 0.00001f; // How small a difference to equate
	//
    // First thing we do is make sure the normals are unit length!
    // It's almost always a good idea to work with pre-normalized normals
    m3dNormalizeVector3(vNorms(0))
    m3dNormalizeVector3(vNorms(1))
    m3dNormalizeVector3(vNorms(2))

    // Search for match - triangle consists of three verts
    for(iVertex <- 0 until 3) {
	  var iMatch = 0;

	  var break = false
	  while(iMatch < nNumVerts && !break) {
		// If the vertex positions are the same
		if(m3dCloseEnough(pVerts(iMatch)(0), verts(iVertex)(0), e) &&
		   m3dCloseEnough(pVerts(iMatch)(1), verts(iVertex)(1), e) &&
		   m3dCloseEnough(pVerts(iMatch)(2), verts(iVertex)(2), e) &&

		   // AND the Normal is the same...
		   m3dCloseEnough(pNorms(iMatch)(0), vNorms(iVertex)(0), e) &&
		   m3dCloseEnough(pNorms(iMatch)(1), vNorms(iVertex)(1), e) &&
		   m3dCloseEnough(pNorms(iMatch)(2), vNorms(iVertex)(2), e) &&

		   // And Texture is the same...
		   m3dCloseEnough(pTexCoords(iMatch)(0), vTexCoords(iVertex)(0), e) &&
		   m3dCloseEnough(pTexCoords(iMatch)(1), vTexCoords(iVertex)(1), e))
		{
		  // Then add the index only
		  pIndexes(nNumIndexes) = iMatch
		  nNumIndexes += 1
		  break = true
		  iMatch -= 1 // To compensate for next addition
		}
		iMatch +=1
	  }

	  // No match for this vertex, add to end of list
	  if(iMatch == nNumVerts && nNumVerts < nMaxIndexes && nNumIndexes < nMaxIndexes) {
		pVerts(nNumVerts).copy(verts(iVertex))
		pNorms(nNumVerts).copy(vNorms(iVertex))
		pTexCoords(nNumVerts).copy(vTexCoords(iVertex))
		pIndexes(nNumIndexes) = nNumVerts

		println("x:" + pVerts(nNumVerts)(0) + " y:" + pVerts(nNumVerts)(1) + " z: " + pVerts(nNumVerts)(2))

		nNumIndexes += 1
		nNumVerts += 1
	  }
	}
  }

  implicit private def floatSeqfToFloatBuffer(floats : Array[Float]) : FloatBuffer = {
	val buffer = BufferUtils.createFloatBuffer(floats.length)
	buffer put floats
	buffer.flip()
	buffer
  }

  implicit private def floatSeqfToIntBuffer(ints : Array[Int]) : IntBuffer = {
	val buffer = BufferUtils.createIntBuffer(ints.length)
	buffer put ints
	buffer.flip()
	buffer
  }

  implicit def vector4fArrayToFloatBuffer (a : Array[M3DVector4f]) : FloatBuffer = {
	val length = 4
	val res = new Array[Float](a.length * length)

	for (i <- 0 until a.length) {
	  Array.copy(a(i).array, 0, res, i*length, length)
	}

	floatSeqfToFloatBuffer(res)
  }
  implicit def vector3fArrayToFloatBuffer (a : Array[M3DVector3f]) : FloatBuffer = {
	val length = 3
	val res = new Array[Float](a.length * length)

	for (i <- 0 until a.length) {
	  Array.copy(a(i).array, 0, res, i*length, length)
	}

	floatSeqfToFloatBuffer(res)
  }
  implicit def vector2fArrayToFloatBuffer (a : Array[M3DVector2f]) : FloatBuffer = {
	val length = 2
	val res = new Array[Float](a.length * length)

	for (i <- 0 until a.length) {
	  Array.copy(a(i).array, 0, res, i*length, length)
	}

	floatSeqfToFloatBuffer(res)
  }
  
  def End(){
	println(nNumVerts)

	// Create the master vertex array object
	vertexArrayBufferObject = glGenVertexArrays();
	glBindVertexArray(vertexArrayBufferObject);

    // Create the buffer objects
    glGenBuffers(bufferObjects);

    // Copy data to video memory
    // Vertex data
    glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.get(VERTEX_DATA));
	glEnableVertexAttribArray(GLT_ATTRIBUTE_VERTEX);
    glBufferData(GL_ARRAY_BUFFER, pVerts, GL_STATIC_DRAW);
	glVertexAttribPointer(GLT_ATTRIBUTE_VERTEX, 3, GL_FLOAT, false, 0, 0);


    // Normal data
    glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.get(NORMAL_DATA));
	glEnableVertexAttribArray(GLT_ATTRIBUTE_NORMAL);
    glBufferData(GL_ARRAY_BUFFER, pNorms, GL_STATIC_DRAW);
	glVertexAttribPointer(GLT_ATTRIBUTE_NORMAL, 3, GL_FLOAT, false, 0, 0);

    // Texture coordinates
    glBindBuffer(GL_ARRAY_BUFFER, bufferObjects.get(TEXTURE_DATA));
	glEnableVertexAttribArray(GLT_ATTRIBUTE_TEXTURE0);
    glBufferData(GL_ARRAY_BUFFER, pTexCoords, GL_STATIC_DRAW);
	glVertexAttribPointer(GLT_ATTRIBUTE_TEXTURE0, 2, GL_FLOAT, false, 0, 0);

    // Indexes
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferObjects.get(INDEX_DATA));
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, pIndexes, GL_STATIC_DRAW);


	// Done
	glBindVertexArray(0);

    // Free older, larger arrays
    // Reasign pointers so they are marked as unused
    pIndexes = null;
    pVerts = null;
    pNorms = null;
    pTexCoords = null;

    // Unbind to anybody
 	glBindVertexArray(0);
  }

  // Useful for statistics
  def GetIndexCount() = nNumIndexes
  def GetVertexCount() = nNumVerts

  // Draw - make sure you call glEnableClientState for these arrays
  override def Draw() {
	glBindVertexArray(vertexArrayBufferObject);

    glDrawElements(GL_TRIANGLES, nNumIndexes, GL_UNSIGNED_INT, 0); // <- WARNING using IInt instead of Short because pIndexes is Array[Int]

    // Unbind to anybody
	glBindVertexArray(vertexArrayBufferObject);
  }

  var pIndexes : Array[Int] = _ // Array of indexes
  var pVerts : Array[M3DVector3f] = _ // Array of vertices
  var pNorms : Array[M3DVector3f] = _ // Array of normals
  var pTexCoords : Array[M3DVector2f] = _ // Array of texture coordinates


  var nMaxIndexes : Int = 0         // Maximum workspace
  var nNumIndexes : Int = 0         // Number of indexes currently used
  var nNumVerts : Int = 0           // Number of vertices actually used

  val bufferObjects : IntBuffer = BufferUtils.createIntBuffer(4)
//  val bufferObjects = new Array[Int](0,0,0,0)
  var vertexArrayBufferObject : Int = 0
}


