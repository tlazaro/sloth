package com.belfrygames.sloth

import java.nio.IntBuffer

final class IntArray (private val buffer : IntBuffer) {
  def this(size : Int) = this(Buffers.createIntBuffer(size))

  @inline final def apply(i : Int) : Int = buffer.get(i)
}

object IntArray {
  implicit def intArrayToIntBuffer(array : IntArray) : IntBuffer = array.buffer
}
