package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D.M3DVector3fArray

/**
 * The BufferPool should provide a way to pool Buffers for Vectors
 * so when those are initialized they should ask the pool for a Buffer
 * instead of just creating a new one. The Vector should notify the
 * Pool in ther finalize() when that memory is again available.
 *
 * The reason is that Vectors need to use Direct buffers to interact
 * with LWJGL and those are really expensive to deallocate. They are
 * not intended to be used for small and reapeated cycles like what
 * would happen during the scene update at several frames per second.
 *
 * Vectors can be of size 2,3 and 4 and matrixes of size 9 and 16.
 * Vectors and matrixes are of type Float or Double, some vectors
 * are of type Int which are the same bytes length as Float.
 */
object BufferPool {
  //  var vector3fPool = new M3DVector3fArray(30)
  //  val floatPool = Buffers.createFloatBuffer(3*4*100)
}
