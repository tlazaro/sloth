package com.belfrygames.sloth.exp.memory

import sun.misc.Unsafe
import java.nio._

package object unsafe {
  val GetUnsafe = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    f.get(null).asInstanceOf[Unsafe]
  }

  def wrapWithByteBuffer(ptr: Long, cap: Int): ByteBuffer = {
    BufferUtils.wrapWithByteBuffer(ptr, cap)
  }

  def wrapWithCharBuffer(ptr: Long, cap: Int): CharBuffer = {
    wrapWithByteBuffer(ptr, cap).asCharBuffer()
  }

  def wrapWithShortBuffer(ptr: Long, cap: Int): ShortBuffer = {
    wrapWithByteBuffer(ptr, cap).asShortBuffer()
  }

  def wrapWithIntBuffer(ptr: Long, cap: Int): IntBuffer = {
    wrapWithByteBuffer(ptr, cap).asIntBuffer()
  }

  def wrapWithLongBuffer(ptr: Long, cap: Int): LongBuffer = {
    wrapWithByteBuffer(ptr, cap).asLongBuffer()
  }

  def wrapWithFloatBuffer(ptr: Long, cap: Int): FloatBuffer = {
    wrapWithByteBuffer(ptr, cap).asFloatBuffer()
  }

  def wrapWithDoubleBuffer(ptr: Long, cap: Int): DoubleBuffer = {
    wrapWithByteBuffer(ptr, cap).asDoubleBuffer()
  }
}
