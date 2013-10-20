package com.belfrygames.sloth.exp.memory.unsafe

trait Memory[@specialized T] {
  def update(i: Long, value: T): Unit

  def apply(i: Long): T

  def size: Long

  def clear()

  override def toString(): String = {
    var res = "Memory[" + apply(0)
    var i = 1
    while (i < size) {
      res += ", " + apply(i)
      i += 1
    }
    res + "]"
  }
}

trait ArrayPtr[@specialized T] {
  def update(i: Long, value: T): Unit

  def apply(i: Long): T

  // TODO Investigate impact of having abstract method address
  def free(): Unit //= GetUnsafe.freeMemory(address)

  // def address: Long
}

object ArrayPtr {
  val BYTE = 1
  val CHAR = 2
  val SHORT = 2
  val INT = 4
  val FLOAT = 4
  val LONG = 8
  val DOUBLE = 8

  def FloatArray(size: Long): UnsafeFloats = UnsafeFloats(GetUnsafe.allocateMemory(size * FLOAT))

  // Copying Array.apply implementation. Not sure how optimal it is...
  def apply(x: Float, xs: Float*) = {
    val array = FloatArray(xs.length + 1)
    array(0) = x
    var i = 1
    for (x <- xs.iterator) {
      array(i) = x; i += 1
    }
    array
  }
}

/**
 * This is an AnyVal, every method here will be inlined. This wrapper will not be present at runtime.
 * @param address
 */
final case class M3DMatrix44f(val address: Long) extends AnyVal {

  import ArrayPtr._

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

  /**
   * Usage is: dst copy src
   * @param src
   */
  def copy(src: M3DMatrix44f): Unit = {
    GetUnsafe.copyMemory(src.address, address, FLOAT * 16)
  }

  /**
   * Other will be treated as a Float Pointer
   * @param offset
   * @param other
   * @param otherOffset
   * @param length
   */
  def copy(offset: Int, other: Long, otherOffset: Int, length: Int): Unit = {
    // TODO investigate how other could be Ptr[Float]
    GetUnsafe.copyMemory(address + FLOAT * offset, other + FLOAT * otherOffset, FLOAT * length)
  }

  override def toString() = {
    "Matrix:(" +
    s"(${this(0)}, ${this(4)}, ${this(8)}, ${this(12)})\n" +
      s"(${this(1)}, ${this(5)}, ${this(9)}, ${this(13)})\n" +
      s"(${this(2)}, ${this(6)}, ${this(10)}, ${this(14)})\n" +
      s"(${this(3)}, ${this(7)}, ${this(11)}, ${this(15)}))"
  }
}

object M3DMatrix44f {

  import ArrayPtr._

  def apply(): M3DMatrix44f = new M3DMatrix44f(GetUnsafe.allocateMemory(16 * FLOAT))

  def array(size: Int): M3DMatrix44fArray = new M3DMatrix44fArray(GetUnsafe.allocateMemory(size * 16 * FLOAT))

  // Copying Array.apply implementation. Not sure how optimal it is...
  def apply(x: Float, xs: Float*): M3DMatrix44f = {
    val matrix = M3DMatrix44f()
    matrix(0) = x
    var i = 1
    for (x <- xs.iterator) {
      matrix(i) = x; i += 1
    }
    matrix
  }
}

/**
 * This is an AnyVal, every method here will be inlined. This wrapper will not be present at runtime.
 * @param address
 */
final case class M3DMatrix33f(val address: Long) extends AnyVal {

  import ArrayPtr._

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

  /**
   * Usage is: dst copy src
   * @param src
   */
  def copy(src: M3DMatrix33f): Unit = {
    GetUnsafe.copyMemory(src.address, address, FLOAT * 9)
  }

  /**
   * Other will be treated as a Float Pointer
   * @param offset
   * @param other
   * @param otherOffset
   * @param length
   */
  def copy(offset: Int, other: Long, otherOffset: Int, length: Int): Unit = {
    // TODO investigate how other could be Ptr[Float]
    GetUnsafe.copyMemory(address + FLOAT * offset, other + FLOAT * otherOffset, FLOAT * length)
  }
}

object M3DMatrix33f {

  import ArrayPtr._

  def apply(): M3DMatrix33f = new M3DMatrix33f(GetUnsafe.allocateMemory(9 * FLOAT))

  def array(size: Int): M3DMatrix33fArray = new M3DMatrix33fArray(GetUnsafe.allocateMemory(size * 9 * FLOAT))

  // Copying Array.apply implementation. Not sure how optimal it is...
  def apply(x: Float, xs: Float*): M3DMatrix33f = {
    val matrix = M3DMatrix33f()
    matrix(0) = x
    var i = 1
    for (x <- xs.iterator) {
      matrix(i) = x; i += 1
    }
    matrix
  }
}

final case class M3DVector3f(val address: Long) extends AnyVal {

  import ArrayPtr._

  def x: Float = this(0)

  def y: Float = this(1)

  def z: Float = this(2)

  def x_=(value: Float) = this(0) = value

  def y_=(value: Float) = this(1) = value

  def z_=(value: Float) = this(2) = value

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

  /**
   * Usage is: dst copy src
   * @param src
   */
  def copy(src: M3DVector3f): Unit = {
    GetUnsafe.copyMemory(src.address, address, FLOAT * 3)
  }

  override def toString() = s"Vector3f:($x, $y, $z)"
}

object M3DVector3f {

  import ArrayPtr._

  def apply(): M3DVector3f = new M3DVector3f(GetUnsafe.allocateMemory(3 * FLOAT))

  def apply(x: Float, y: Float, z: Float): M3DVector3f = {
    val v = M3DVector3f()
    v(0) = x
    v(1) = y
    v(2) = z
    v
  }
}

final case class M3DVector4f(val address: Long) extends AnyVal {

  import ArrayPtr._

  def x: Float = this(0)

  def y: Float = this(1)

  def z: Float = this(2)

  def w: Float = this(3)

  def x_=(value: Float) = this(0) = value

  def y_=(value: Float) = this(1) = value

  def z_=(value: Float) = this(2) = value

  def w_=(value: Float) = this(3) = value

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

  /**
   * Usage is: dst copy src
   * @param src
   */
  def copy(src: M3DVector4f): Unit = {
    GetUnsafe.copyMemory(src.address, address, FLOAT * 4)
  }

  override def toString() = s"Vector4f:($x, $y, $z, $w)"
}

object M3DVector4f {

  import ArrayPtr._

  def apply(): M3DVector4f = new M3DVector4f(GetUnsafe.allocateMemory(4 * FLOAT))

  def apply(x: Float, y: Float, z: Float, w: Float): M3DVector4f = {
    val v = M3DVector4f()
    v(0) = x
    v(1) = y
    v(2) = z
    v(3) = w
    v
  }
}

case class M3DMatrix44fArray(val address: Long) extends AnyVal {

  import ArrayPtr._

  def update(i: Long, value: M3DMatrix44f): Unit = this(i) copy value

  def apply(idx: Long): M3DMatrix44f = new M3DMatrix44f(address + idx * 16 * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

}

case class M3DMatrix33fArray(val address: Long) extends AnyVal {

  import ArrayPtr._

  def update(i: Long, value: M3DMatrix33f): Unit = this(i) copy value

  def apply(idx: Long): M3DMatrix33f = new M3DMatrix33f(address + idx * 9 * FLOAT)

  def free() = GetUnsafe.freeMemory(address)

}

/**
 * This is an AnyVal, every method here will be inlined. This wrapper will not be present at runtime.
 * @param address
 */
case class UnsafeFloats(val address: Long) extends AnyVal {
  /* TODO Investigate impact of having trait here: with ArrayPtr[Float] { */

  import ArrayPtr._

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def free() = GetUnsafe.freeMemory(address)
}

class UnsafeByteArray(val size: Long) extends Memory[Byte] {

  import ArrayPtr._

  val address = GetUnsafe.allocateMemory(size * BYTE)

  def update(i: Long, value: Byte): Unit = GetUnsafe.putByte(address + i * BYTE, value)

  def apply(idx: Long) = GetUnsafe.getByte(address + idx * BYTE)

  def clear() = GetUnsafe.setMemory(address, size * BYTE, 0)
}

object UnsafeFloatArray {
  // Copying Array.apply implementation. Not sure how optimal it is...
  def apply(x: Float, xs: Float*): UnsafeFloatArray = {
    val array = new UnsafeFloatArray(xs.length + 1)
    array(0) = x
    var i = 1
    for (x <- xs.iterator) {
      array(i) = x; i += 1
    }
    array
  }
}

class UnsafeFloatArray(val size: Long) extends Memory[Float] {

  def this(size: Long, clean: Boolean) = {
    this(size)
    if (clean) clear()
  }

  import ArrayPtr._

  val address = GetUnsafe.allocateMemory(size * FLOAT)

  def update(i: Long, value: Float): Unit = GetUnsafe.putFloat(address + i * FLOAT, value)

  def apply(idx: Long) = GetUnsafe.getFloat(address + idx * FLOAT)

  def clear() = GetUnsafe.setMemory(address, size * FLOAT, 0)
}

class UnsafeIntArray(val size: Long) extends Memory[Int] {

  import ArrayPtr._

  val address = GetUnsafe.allocateMemory(size * INT)

  def update(i: Long, value: Int): Unit = GetUnsafe.putInt(address + i * INT, value)

  def apply(idx: Long) = GetUnsafe.getInt(address + idx * INT)

  def clear() = GetUnsafe.setMemory(address, size * INT, 0)
}