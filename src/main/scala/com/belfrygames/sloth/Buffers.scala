package com.belfrygames.sloth

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer
import java.nio.ShortBuffer

object Buffers {
  /**
   * Construct a direct native-ordered bytebuffer with the specified size.
   * @param size The size, in bytes
   * @return a ByteBuffer
   */
  def createByteBuffer(size : Int, direct : Boolean = true) : ByteBuffer = if (direct) {
	ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
  } else {
	ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
  }

  /**
   * Construct a direct native-order shortbuffer with the specified number
   * of elements.
   * @param size The size, in shorts
   * @return a ShortBuffer
   */
  def createShortBuffer(size : Int, direct : Boolean = true) : ShortBuffer = {
	return createByteBuffer(size << 1, direct).asShortBuffer();
  }

  /**
   * Construct a direct native-order charbuffer with the specified number
   * of elements.
   * @param size The size, in chars
   * @return an CharBuffer
   */
  def createCharBuffer(size : Int, direct : Boolean = true) : CharBuffer = {
	return createByteBuffer(size << 1, direct).asCharBuffer();
  }

  /**
   * Construct a direct native-order intbuffer with the specified number
   * of elements.
   * @param size The size, in ints
   * @return an IntBuffer
   */
  def createIntBuffer(size : Int, direct : Boolean = true) : IntBuffer = {
	return createByteBuffer(size << 2, direct).asIntBuffer();
  }

  /**
   * Construct a direct native-order longbuffer with the specified number
   * of elements.
   * @param size The size, in longs
   * @return an LongBuffer
   */
  def createLongBuffer(size : Int, direct : Boolean = true) : LongBuffer = {
	return createByteBuffer(size << 3, direct).asLongBuffer();
  }

  /**
   * Construct a direct native-order floatbuffer with the specified number
   * of elements.
   * @param size The size, in floats
   * @return a FloatBuffer
   */
  def createFloatBuffer(size : Int, direct : Boolean = true) : FloatBuffer = {
	return createByteBuffer(size << 2, direct).asFloatBuffer();
  }

  /**
   * Construct a direct native-order doublebuffer with the specified number
   * of elements.
   * @param size The size, in floats
   * @return a FloatBuffer
   */
  def createDoubleBuffer(size : Int, direct : Boolean = true) : DoubleBuffer = {
	return createByteBuffer(size << 3, direct).asDoubleBuffer();
  }
}
