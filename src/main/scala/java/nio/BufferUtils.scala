package java.nio

import sun.nio.ch.DirectBuffer
import sun.misc.{Unsafe, Cleaner}

object BufferUtils {
  def wrapWithByteBuffer(ptr: Long, cap: Int): ByteBuffer = {
    new MyDirectByteBuffer(ptr, cap)
  }
}

object MyDirectByteBuffer {
  val unsafe: Unsafe = Bits.unsafe
  val arrayBaseOffset: Long = unsafe.arrayBaseOffset(classOf[Array[Byte]]).asInstanceOf[Long]
  val unaligned: Boolean = Bits.unaligned

  def checkBounds(off: Int, len: Int, size: Int) {
    if ((off | len | (off + len) | (size - (off + len))) < 0) throw new IndexOutOfBoundsException
  }
}

/**
 * Does not allocate, does not wipe memory, just wraps.
 * @param _address
 * @param cap
 */
class MyDirectByteBuffer(_address: Long, cap: Int) extends MappedByteBuffer(-1, 0, cap, cap) with DirectBuffer {
  getClass.getField("address").set(this, _address)

  @inline def address(): Long = _address

  import MyDirectByteBuffer._

  def slice: ByteBuffer = {
    val pos: Int = this.position
    val lim: Int = this.limit
    assert((pos <= lim))
    val rem: Int = (if (pos <= lim) lim - pos else 0)
    val off: Int = (pos << 0)
    assert((off >= 0))
    return new DirectByteBuffer(this, -1, 0, rem, rem, off)
  }

  def duplicate: ByteBuffer = {
    return new DirectByteBuffer(this, this.markValue, this.position, this.limit, this.capacity, 0)
  }

  def asReadOnlyBuffer: ByteBuffer = {
    return new DirectByteBufferR(this, this.markValue, this.position, this.limit, this.capacity, 0)
  }

  private def ix(i: Int): Long = {
    return _address + (i << 0)
  }

  def get: Byte = {
    return ((unsafe.getByte(ix(nextGetIndex))))
  }

  def get(i: Int): Byte = {
    return ((unsafe.getByte(ix(checkIndex(i)))))
  }

  override def get(dst: Array[Byte], offset: Int, length: Int): ByteBuffer = {
    if ((length << 0) > Bits.JNI_COPY_TO_ARRAY_THRESHOLD) {
      checkBounds(offset, length, dst.length)
      val pos: Int = position
      val lim: Int = limit
      assert((pos <= lim))
      val rem: Int = (if (pos <= lim) lim - pos else 0)
      if (length > rem) throw new BufferUnderflowException
      Bits.copyToArray(ix(pos), dst, arrayBaseOffset, offset << 0, length << 0)
      position(pos + length)
    }
    else {
      super.get(dst, offset, length)
    }
    return this
  }

  def put(x: Byte): ByteBuffer = {
    unsafe.putByte(ix(nextPutIndex), ((x)))
    return this
  }

  def put(i: Int, x: Byte): ByteBuffer = {
    unsafe.putByte(ix(checkIndex(i)), ((x)))
    return this
  }

  override def put(src: ByteBuffer): ByteBuffer = {
    if (src.isInstanceOf[MyDirectByteBuffer]) {
      if (src eq this) throw new IllegalArgumentException
      val sb: MyDirectByteBuffer = src.asInstanceOf[MyDirectByteBuffer]
      val spos: Int = sb.position
      val slim: Int = sb.limit
      assert((spos <= slim))
      val srem: Int = (if (spos <= slim) slim - spos else 0)
      val pos: Int = position
      val lim: Int = limit
      assert((pos <= lim))
      val rem: Int = (if (pos <= lim) lim - pos else 0)
      if (srem > rem) throw new BufferOverflowException
      unsafe.copyMemory(sb.ix(spos), ix(pos), srem << 0)
      sb.position(spos + srem)
      position(pos + srem)
    }
    else if (src.hb != null) {
      val spos: Int = src.position
      val slim: Int = src.limit
      assert((spos <= slim))
      val srem: Int = (if (spos <= slim) slim - spos else 0)
      put(src.hb, src.offset + spos, srem)
      src.position(spos + srem)
    }
    else {
      super.put(src)
    }
    return this
  }

  override def put(src: Array[Byte], offset: Int, length: Int): ByteBuffer = {
    if ((length << 0) > Bits.JNI_COPY_FROM_ARRAY_THRESHOLD) {
      checkBounds(offset, length, src.length)
      val pos: Int = position
      val lim: Int = limit
      assert((pos <= lim))
      val rem: Int = (if (pos <= lim) lim - pos else 0)
      if (length > rem) throw new BufferOverflowException
      Bits.copyFromArray(src, arrayBaseOffset, offset << 0, ix(pos), length << 0)
      position(pos + length)
    }
    else {
      super.put(src, offset, length)
    }
    return this
  }

  def compact: ByteBuffer = {
    val pos: Int = position
    val lim: Int = limit
    assert((pos <= lim))
    val rem: Int = (if (pos <= lim) lim - pos else 0)
    unsafe.copyMemory(ix(pos), ix(0), rem << 0)
    position(rem)
    limit(capacity)
    discardMark
    return this
  }

  def isDirect: Boolean = true

  def isReadOnly: Boolean = false

  private[nio] def _get(i: Int): Byte = {
    return unsafe.getByte(_address + i)
  }

  private[nio] def _put(i: Int, b: Byte) {
    unsafe.putByte(_address + i, b)
  }

  private def getChar(a: Long): Char = {
    if (unaligned) {
      val x: Char = unsafe.getChar(a)
      return (if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getChar(a, bigEndian)
  }

  def getChar: Char = {
    return getChar(ix(nextGetIndex((1 << 1))))
  }

  def getChar(i: Int): Char = {
    return getChar(ix(checkIndex(i, (1 << 1))))
  }

  private def putChar(a: Long, x: Char): ByteBuffer = {
    if (unaligned) {
      val y: Char = (x)
      unsafe.putChar(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putChar(a, x, bigEndian)
    }
    return this
  }

  def putChar(x: Char): ByteBuffer = {
    putChar(ix(nextPutIndex((1 << 1))), x)
    return this
  }

  def putChar(i: Int, x: Char): ByteBuffer = {
    putChar(ix(checkIndex(i, (1 << 1))), x)
    return this
  }

  def asCharBuffer: CharBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 1
    if (!unaligned && ((_address + off) % (1 << 1) != 0)) {
      return (if (bigEndian) (new ByteBufferAsCharBufferB(this, -1, 0, size, size, off)).asInstanceOf[CharBuffer] else (new ByteBufferAsCharBufferL(this, -1, 0, size, size, off)).asInstanceOf[CharBuffer])
    }
    else {
      return (if (nativeByteOrder) (new DirectCharBufferU(this, -1, 0, size, size, off)).asInstanceOf[CharBuffer] else (new DirectCharBufferS(this, -1, 0, size, size, off)).asInstanceOf[CharBuffer])
    }
  }

  private def getShort(a: Long): Short = {
    if (unaligned) {
      val x: Short = unsafe.getShort(a)
      return (if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getShort(a, bigEndian)
  }

  def getShort: Short = {
    return getShort(ix(nextGetIndex((1 << 1))))
  }

  def getShort(i: Int): Short = {
    return getShort(ix(checkIndex(i, (1 << 1))))
  }

  private def putShort(a: Long, x: Short): ByteBuffer = {
    if (unaligned) {
      val y: Short = (x)
      unsafe.putShort(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putShort(a, x, bigEndian)
    }
    return this
  }

  def putShort(x: Short): ByteBuffer = {
    putShort(ix(nextPutIndex((1 << 1))), x)
    return this
  }

  def putShort(i: Int, x: Short): ByteBuffer = {
    putShort(ix(checkIndex(i, (1 << 1))), x)
    return this
  }

  def asShortBuffer: ShortBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 1
    if (!unaligned && ((_address + off) % (1 << 1) != 0)) {
      return (if (bigEndian) (new ByteBufferAsShortBufferB(this, -1, 0, size, size, off)).asInstanceOf[ShortBuffer] else (new ByteBufferAsShortBufferL(this, -1, 0, size, size, off)).asInstanceOf[ShortBuffer])
    }
    else {
      return (if (nativeByteOrder) (new DirectShortBufferU(this, -1, 0, size, size, off)).asInstanceOf[ShortBuffer] else (new DirectShortBufferS(this, -1, 0, size, size, off)).asInstanceOf[ShortBuffer])
    }
  }

  private def getInt(a: Long): Int = {
    if (unaligned) {
      val x: Int = unsafe.getInt(a)
      return (if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getInt(a, bigEndian)
  }

  def getInt: Int = {
    return getInt(ix(nextGetIndex((1 << 2))))
  }

  def getInt(i: Int): Int = {
    return getInt(ix(checkIndex(i, (1 << 2))))
  }

  private def putInt(a: Long, x: Int): ByteBuffer = {
    if (unaligned) {
      val y: Int = (x)
      unsafe.putInt(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putInt(a, x, bigEndian)
    }
    return this
  }

  def putInt(x: Int): ByteBuffer = {
    putInt(ix(nextPutIndex((1 << 2))), x)
    return this
  }

  def putInt(i: Int, x: Int): ByteBuffer = {
    putInt(ix(checkIndex(i, (1 << 2))), x)
    return this
  }

  def asIntBuffer: IntBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 2
    if (!unaligned && ((_address + off) % (1 << 2) != 0)) {
      return (if (bigEndian) (new ByteBufferAsIntBufferB(this, -1, 0, size, size, off)).asInstanceOf[IntBuffer] else (new ByteBufferAsIntBufferL(this, -1, 0, size, size, off)).asInstanceOf[IntBuffer])
    }
    else {
      return (if (nativeByteOrder) (new DirectIntBufferU(this, -1, 0, size, size, off)).asInstanceOf[IntBuffer] else (new DirectIntBufferS(this, -1, 0, size, size, off)).asInstanceOf[IntBuffer])
    }
  }

  private def getLong(a: Long): Long = {
    if (unaligned) {
      val x: Long = unsafe.getLong(a)
      return (if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getLong(a, bigEndian)
  }

  def getLong: Long = {
    return getLong(ix(nextGetIndex((1 << 3))))
  }

  def getLong(i: Int): Long = {
    return getLong(ix(checkIndex(i, (1 << 3))))
  }

  private def putLong(a: Long, x: Long): ByteBuffer = {
    if (unaligned) {
      val y: Long = (x)
      unsafe.putLong(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putLong(a, x, bigEndian)
    }
    return this
  }

  def putLong(x: Long): ByteBuffer = {
    putLong(ix(nextPutIndex((1 << 3))), x)
    return this
  }

  def putLong(i: Int, x: Long): ByteBuffer = {
    putLong(ix(checkIndex(i, (1 << 3))), x)
    return this
  }

  def asLongBuffer: LongBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 3
    if (!unaligned && ((_address + off) % (1 << 3) != 0)) {
      return (if (bigEndian) (new ByteBufferAsLongBufferB(this, -1, 0, size, size, off)).asInstanceOf[LongBuffer] else (new ByteBufferAsLongBufferL(this, -1, 0, size, size, off)).asInstanceOf[LongBuffer])
    }
    else {
      return (if (nativeByteOrder) (new DirectLongBufferU(this, -1, 0, size, size, off)).asInstanceOf[LongBuffer] else (new DirectLongBufferS(this, -1, 0, size, size, off)).asInstanceOf[LongBuffer])
    }
  }

  private def getFloat(a: Long): Float = {
    if (unaligned) {
      val x: Int = unsafe.getInt(a)
      return java.lang.Float.intBitsToFloat(if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getFloat(a, bigEndian)
  }

  def getFloat: Float = {
    return getFloat(ix(nextGetIndex((1 << 2))))
  }

  def getFloat(i: Int): Float = {
    return getFloat(ix(checkIndex(i, (1 << 2))))
  }

  private def putFloat(a: Long, x: Float): ByteBuffer = {
    if (unaligned) {
      val y: Int = java.lang.Float.floatToRawIntBits(x)
      unsafe.putInt(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putFloat(a, x, bigEndian)
    }
    return this
  }

  def putFloat(x: Float): ByteBuffer = {
    putFloat(ix(nextPutIndex((1 << 2))), x)
    return this
  }

  def putFloat(i: Int, x: Float): ByteBuffer = {
    putFloat(ix(checkIndex(i, (1 << 2))), x)
    return this
  }

  def asFloatBuffer: FloatBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 2
    if (!unaligned && ((_address + off) % (1 << 2) != 0)) {
      return (if (bigEndian) (new ByteBufferAsFloatBufferB(this, -1, 0, size, size, off)).asInstanceOf[FloatBuffer] else (new ByteBufferAsFloatBufferL(this, -1, 0, size, size, off)).asInstanceOf[FloatBuffer])
    }
    else {
      return (if (nativeByteOrder) (new DirectFloatBufferU(this, -1, 0, size, size, off)).asInstanceOf[FloatBuffer] else (new DirectFloatBufferS(this, -1, 0, size, size, off)).asInstanceOf[FloatBuffer])
    }
  }

  private def getDouble(a: Long): Double = {
    if (unaligned) {
      val x: Long = unsafe.getLong(a)
      return java.lang.Double.longBitsToDouble(if (nativeByteOrder) x else Bits.swap(x))
    }
    return Bits.getDouble(a, bigEndian)
  }

  def getDouble: Double = {
    return getDouble(ix(nextGetIndex((1 << 3))))
  }

  def getDouble(i: Int): Double = {
    return getDouble(ix(checkIndex(i, (1 << 3))))
  }

  private def putDouble(a: Long, x: Double): ByteBuffer = {
    if (unaligned) {
      val y: Long = java.lang.Double.doubleToRawLongBits(x)
      unsafe.putLong(a, (if (nativeByteOrder) y else Bits.swap(y)))
    }
    else {
      Bits.putDouble(a, x, bigEndian)
    }
    return this
  }

  def putDouble(x: Double): ByteBuffer = {
    putDouble(ix(nextPutIndex((1 << 3))), x)
    return this
  }

  def putDouble(i: Int, x: Double): ByteBuffer = {
    putDouble(ix(checkIndex(i, (1 << 3))), x)
    return this
  }

  def asDoubleBuffer: DoubleBuffer = {
    val off: Int = this.position
    val lim: Int = this.limit
    assert((off <= lim))
    val rem: Int = (if (off <= lim) lim - off else 0)
    val size: Int = rem >> 3
    if (!unaligned && ((_address + off) % (1 << 3) != 0)) {
      return (if (bigEndian) (new ByteBufferAsDoubleBufferB(this, -1, 0, size, size, off)).asInstanceOf[DoubleBuffer] else (new ByteBufferAsDoubleBufferL(this, -1, 0, size, size, off)).asInstanceOf[DoubleBuffer])
    } else {
      return (if (nativeByteOrder) (new DirectDoubleBufferU(this, -1, 0, size, size, off)).asInstanceOf[DoubleBuffer] else (new DirectDoubleBufferS(this, -1, 0, size, size, off)).asInstanceOf[DoubleBuffer])
    }
  }

  def attachment(): AnyRef = null

  def cleaner(): Cleaner = null
}