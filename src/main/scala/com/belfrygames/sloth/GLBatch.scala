package com.belfrygames.sloth

import com.belfrygames.sloth.GLShaderManager._
import com.belfrygames.sloth.Math3D.M3DVector2f
import com.belfrygames.sloth.Math3D.M3DVector3f
import com.belfrygames.sloth.Math3D.M3DVector4f
import com.belfrygames.sloth.Math3D.M3DVector2fArray
import com.belfrygames.sloth.Math3D.M3DVector3fArray
import com.belfrygames.sloth.Math3D.M3DVector4fArray
import java.nio.FloatBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30._


class GLBatch extends GLBatchBase {
  import GLBatch._

  protected var primitiveType = 0		// What am I drawing....

  protected var uiVertexArray : Int = _
  protected var uiNormalArray : Int = _
  protected var uiColorArray : Int = _
  protected var uiTextureCoordArray : Array[Int] = _
  protected var vertexArrayObject = 0

  protected var nVertsBuilding = 0			// Building up vertexes counter (immediate mode emulator)
  protected var nNumVerts = 0				// Number of verticies in this batch
  protected var nNumTextureUnits = 0		// Number of texture coordinate sets

  protected var	bBatchDone = false;				// Batch has been built

  protected var pVerts : M3DVector3fArray = null // Array of vertices
  protected var pNormals : M3DVector3fArray = null // Array of normals
  protected var pColors : M3DVector4fArray = null // Array of colors
  protected var pTexCoords : Array[M3DVector2fArray] = null // Array of texture coordinates

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

		glDeleteVertexArrays(vertexArrayObject);
  }

  // Start populating the array
  def Begin(primitive : Int, nVerts : Int, nTextureUnits : Int = 0) {
		primitiveType = primitive
		nNumVerts = nVerts

		nNumTextureUnits = if(nTextureUnits > 4) 4 else nTextureUnits

		if(nNumTextureUnits != 0) {
			uiTextureCoordArray = new Array(nNumTextureUnits)

			// An array of pointers to texture coordinate arrays
			pTexCoords = new Array[M3DVector2fArray](nNumTextureUnits)
			for(i <- 0 until nNumTextureUnits) {
				uiTextureCoordArray(i) = 0
			}
		}

		// Vertex Array object for this Array
		vertexArrayObject = glGenVertexArrays()
		glBindVertexArray(vertexArrayObject)
  }

  // Tell the batch you are done
  def End() {
		// Check to see if items have been added one at a time
		if(pVerts != null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)
			glUnmapBuffer(GL_ARRAY_BUFFER)
			pVerts = null
		}

		if(pColors != null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)
			glUnmapBuffer(GL_ARRAY_BUFFER)
			pColors = null
		}

		if(pNormals != null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)
			glUnmapBuffer(GL_ARRAY_BUFFER)
			pNormals = null
		}

		for(i <- 0 until nNumTextureUnits) {
			if(pTexCoords(i) != null) {
				glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(i))
				glUnmapBuffer(GL_ARRAY_BUFFER)
				pTexCoords(i) = null
			}
		}

		// Set up the vertex array object
		glBindVertexArray(vertexArrayObject)

		if(uiVertexArray != 0) {
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


  def CopyVertexData3f(vVerts : FloatBuffer) {
		// First time, create the buffer object, allocate the space
		if(uiVertexArray == 0) {
			uiVertexArray = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)
			glBufferData(GL_ARRAY_BUFFER, vVerts, GL_DYNAMIC_DRAW)
		} else { // Just bind to existing object
			glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray)

			// Copy the data in
			glBufferSubData(GL_ARRAY_BUFFER, 0, vVerts)
			pVerts = null
		}
  }

  def CopyVertexData3f(vVerts : M3DVector3fArray) : Unit = CopyVertexData3f(vVerts.slice(nNumVerts))

  def CopyNormalDataf(vNorms : FloatBuffer) {
		// First time, create the buffer object, allocate the space
		if(uiNormalArray == 0) {
			uiNormalArray = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)
			glBufferData(GL_ARRAY_BUFFER, vNorms, GL_DYNAMIC_DRAW)
		} else {	// Just bind to existing object
			glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray)

			// Copy the data in
			glBufferSubData(GL_ARRAY_BUFFER, 0, vNorms)
			pNormals = null
		}
  }

  def CopyColorData4f(vColors : FloatBuffer) {
		// First time, create the buffer object, allocate the space
		if(uiColorArray == 0) {
			uiColorArray = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)
			glBufferData(GL_ARRAY_BUFFER, vColors, GL_DYNAMIC_DRAW)
		} else {	// Just bind to existing object
			glBindBuffer(GL_ARRAY_BUFFER, uiColorArray)

			// Copy the data in
			glBufferSubData(GL_ARRAY_BUFFER, 0, vColors)
			pColors = null
		}
  }

  def CopyTexCoordData2f(vTexCoords : FloatBuffer, uiTextureLayer : Int) {
		// First time, create the buffer object, allocate the space
		if(uiTextureCoordArray(uiTextureLayer) == 0) {
			uiTextureCoordArray(uiTextureLayer) = glGenBuffers()
			glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(uiTextureLayer))
			glBufferData(GL_ARRAY_BUFFER, vTexCoords, GL_DYNAMIC_DRAW)
		} else {	// Just bind to existing object
			glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(uiTextureLayer))

			// Copy the data in
			glBufferSubData(GL_ARRAY_BUFFER, 0, vTexCoords)
			pTexCoords(uiTextureLayer) = null
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

  private def setupVertex3f () {
		// First see if the vertex array buffer has been created...
		if(uiVertexArray == 0) {	// Nope, we need to create it
			uiVertexArray = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray);
			glBufferData(GL_ARRAY_BUFFER, 4 * 3 * nNumVerts, GL_DYNAMIC_DRAW);
		}

		// Now see if it's already mapped, if not, map it
		if(pVerts == null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiVertexArray);
			pVerts = new M3DVector3fArray(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, null).asFloatBuffer)
		}
  }

  def Vertex3f(x : Float, y : Float, z : Float) {
		setupVertex3f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pVerts(nVertsBuilding)(0) = x;
		pVerts(nVertsBuilding)(1) = y;
		pVerts(nVertsBuilding)(2) = z;
		nVertsBuilding += 1;
  }
  def Vertex3fv(vVertex : M3DVector3f) {
		setupVertex3f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pVerts(nVertsBuilding).copy(vVertex)
		nVertsBuilding += 1;
  }

  private def setupNormal3f () {
		// First see if the vertex array buffer has been created...
		if(uiNormalArray == 0) {	// Nope, we need to create it
			uiNormalArray = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray);
			glBufferData(GL_ARRAY_BUFFER, 4 * 3 * nNumVerts, GL_DYNAMIC_DRAW);
		}

		// Now see if it's already mapped, if not, map it
		if(pNormals == null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiNormalArray);
			pNormals = new M3DVector3fArray(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, null).asFloatBuffer)
		}
  }

  def Normal3f(x : Float, y : Float, z : Float) {
		setupNormal3f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pNormals(nVertsBuilding)(0) = x;
		pNormals(nVertsBuilding)(1) = y;
		pNormals(nVertsBuilding)(2) = z;
  }

  def Normal3fv(vNormal : M3DVector3f) {
		setupNormal3f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pNormals(nVertsBuilding).copy(vNormal)
  }

  private def setupColor4f () {
		// First see if the vertex array buffer has been created...
		if(uiColorArray == 0) {	// Nope, we need to create it
			uiColorArray = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, uiColorArray);
			glBufferData(GL_ARRAY_BUFFER, 4 * 4 * nNumVerts, GL_DYNAMIC_DRAW);
		}

		// Now see if it's already mapped, if not, map it
		if(pColors == null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiColorArray);
			pColors = new M3DVector4fArray(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, null).asFloatBuffer)
		}
  }

  def Color4f(r : Float, g : Float, b : Float, a : Float) {
		setupColor4f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pColors(nVertsBuilding)(0) = r;
		pColors(nVertsBuilding)(1) = g;
		pColors(nVertsBuilding)(2) = b;
		pColors(nVertsBuilding)(3) = a;
  }

  def Color4fv(vColor : M3DVector4f) {
		setupColor4f()

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pColors(nVertsBuilding).copy(vColor)
  }

  private def setupMultiTexCoord2f(texture : Int) {
		// First see if the vertex array buffer has been created...
		if(uiTextureCoordArray(texture) == 0) {	// Nope, we need to create it
			uiTextureCoordArray(texture) = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(texture));
			glBufferData(GL_ARRAY_BUFFER, 4 * 2 * nNumVerts, GL_DYNAMIC_DRAW);
		}

		// Now see if it's already mapped, if not, map it
		if(pTexCoords(texture) == null) {
			glBindBuffer(GL_ARRAY_BUFFER, uiTextureCoordArray(texture));
			pTexCoords(texture) = new M3DVector2fArray(glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY, null).asFloatBuffer)
		}
  }

  def MultiTexCoord2f(texture : Int, s : Float, t : Float) {
		setupMultiTexCoord2f(texture)

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pTexCoords(texture)(nVertsBuilding)(0) = s;
		pTexCoords(texture)(nVertsBuilding)(1) = t;
  }

  def MultiTexCoord2fv(texture : Int, vTexCoord : M3DVector2f) {
		setupMultiTexCoord2f(texture)

		// Ignore if we go past the end, keeps things from blowing up
		if(nVertsBuilding >= nNumVerts)
			return;

		// Copy it in...
		pTexCoords(texture)(nVertsBuilding).copy(vTexCoord)
  }
}

object GLBatch {
  implicit def getFloatBuffer(a : Array[Float]) : FloatBuffer = {
		val buffer = BufferUtils.createFloatBuffer(a.length)
		buffer.put(a)
		buffer.flip()
		buffer
  }
}