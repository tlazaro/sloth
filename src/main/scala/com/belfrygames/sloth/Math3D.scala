package com.belfrygames.sloth

import java.nio.IntBuffer
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.Buffer

object Math3D {
  import scala.math._
  
  // Math3d.h
  // // Math3D Library, version 0.95

  /* Copyright (c) 2009, Richard S. Wright Jr.
   All rights reserved.

   Redistribution and use in source and binary forms, with or without modification,
   are permitted provided that the following conditions are met:

   Redistributions of source code must retain the above copyright notice, this list
   of conditions and the following disclaimer.

   Redistributions in binary form must reproduce the above copyright notice, this list
   of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.

   Neither the name of Richard S. Wright Jr. nor the names of other contributors may be used
   to endorse or promote products derived from this software without specific prior
   written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
   SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
   TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
   CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
   ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   */

// Header file for the Math3d library. The C-Runtime has math.h, this file and the
// accompanying math3d.cpp are meant to suppliment math.h by adding geometry/math routines
// useful for graphics, simulation, and physics applications (3D stuff).
// This library is meant to be useful on Win32, Mac OS X, various Linux/Unix distros,
// and mobile platforms. Although designed with OpenGL in mind, there are no OpenGL
// dependencies. Other than standard math routines, the only other outside routine
// used is memcpy (for faster copying of vector arrays).
// Richard S. Wright Jr.

// Data structures and containers
// Much thought went into how these are declared. Many libraries declare these
// as structures with x, y, z data members. However structure alignment issues
// could limit the portability of code based on such structures, or the binary
// compatibility of data files (more likely) that contain such structures across
// compilers/platforms. Arrays are always tightly packed, and are more efficient
// for moving blocks of data around (usually).
// Sigh... yes, I probably should use GLfloat, etc. But that requires that we
// always include OpenGL. Since this library is also useful for non-graphical
// applications, I shall risk the wrath of the portability gods...
//

  // TODO somehow test if this is working, check .class files and see what happened
  sealed trait M3DVector[T] {
	@inline def array : Buffer

	@inline def apply(i : Int) : T
	@inline def update(i : Int, x : T) : Unit
	@inline final def size() : Int = array.capacity

	@inline def copy(other : Array[T]) : Unit
	@inline def copy(other : M3DVector[T]) : Unit
	@inline def copy(offset : Int, other : M3DVector[T], otherOffset : Int, length : Int) : Unit

	override def toString() : String = {
	  var res = "Vector[" + apply(0)
	  var i = 1
	  while (i < size) {
		res += ", " + apply(i)
		i += 1
	  }
	  res + "]"
	}
  }

  sealed trait M3DVectorFloat extends M3DVector[Float] {
	@inline final def apply(i : Int) : Float = array.asInstanceOf[FloatBuffer].get(i)
	@inline final def update(i : Int, x : Float) : Unit = array.asInstanceOf[FloatBuffer].put(i, x)
	@inline final def copy(other : Array[Float]) : Unit = {
	  array.position(0)
	  array.asInstanceOf[FloatBuffer].put(other)
	}
	@inline final def copy(other : M3DVector[Float]) : Unit = {
	  array.position(0)
	  other.array.position(0)
	  array.asInstanceOf[FloatBuffer].put(other.array.asInstanceOf[FloatBuffer])
	}

	@inline final def copy(offset : Int, other : M3DVector[Float], otherOffset : Int, length : Int) : Unit = {
	  array.position(offset)
	  other.array.position(otherOffset)
	  other.array.limit(otherOffset + length)
	  array.asInstanceOf[FloatBuffer].put(other.array.asInstanceOf[FloatBuffer])
	  other.array.clear()
	}
  }
  sealed trait M3DVectorDouble extends M3DVector[Double] {
	@inline final def apply(i : Int) : Double = array.asInstanceOf[DoubleBuffer].get(i)
	@inline final def update(i : Int, x : Double) : Unit = array.asInstanceOf[DoubleBuffer].put(i, x)
	@inline final def copy(other : Array[Double]) : Unit = {
	  array.position(0)
	  array.asInstanceOf[DoubleBuffer].put(other)
	}
	@inline final def copy(other : M3DVector[Double]) : Unit = {
	  array.position(0)
	  other.array.position(0)
	  array.asInstanceOf[FloatBuffer].put(other.array.asInstanceOf[FloatBuffer])
	}

	@inline final def copy(offset : Int, other : M3DVector[Double], otherOffset : Int, length : Int) : Unit = {
	  array.position(offset)
	  other.array.position(otherOffset)
	  other.array.limit(otherOffset + length)
	  array.asInstanceOf[DoubleBuffer].put(other.array.asInstanceOf[DoubleBuffer])
	  other.array.clear()
	}
  }
  sealed trait M3DVectorInt extends M3DVector[Int] {
	@inline final def apply(i : Int) : Int = array.asInstanceOf[IntBuffer].get(i)
	@inline final def update(i : Int, x : Int) : Unit = array.asInstanceOf[IntBuffer].put(i, x)
	@inline final def copy(other : Array[Int]) : Unit = {
	  array.position(0)
	  array.asInstanceOf[IntBuffer].put(other)
	}
	@inline final def copy(other : M3DVector[Int]) : Unit = {
	  array.position(0)
	  other.array.position(0)
	  array.asInstanceOf[IntBuffer].put(other.array.asInstanceOf[IntBuffer])
	}

	@inline final def copy(offset : Int, other : M3DVector[Int], otherOffset : Int, length : Int) : Unit = {
	  array.position(offset)
	  other.array.position(otherOffset)
	  other.array.limit(otherOffset + length)
	  array.asInstanceOf[IntBuffer].put(other.array.asInstanceOf[IntBuffer])
	  other.array.clear()
	}
  }

  object M3DVector {
	def apply(x : Float, y : Float, z : Float, w : Float) = {
	  val vec = new M3DVector4f
	  vec(0) = x
	  vec(1) = y
	  vec(2) = z
	  vec(3) = w
	  vec
	}

	def apply(x : Float, y : Float, z : Float) = {
	  val vec = new M3DVector3f
	  vec(0) = x
	  vec(1) = y
	  vec(2) = z
	  vec
	}

	// Write and Read to Vector don't depend on buffer position but reading the buffer directly needs position reset.
	// This is the best place to do so because it's the only place that will be needed
	implicit def M3DVectorIntToBuffer(vec : M3DVectorInt) = {vec.array.position(0); vec.array.asInstanceOf[IntBuffer]}
	implicit def M3DVectorFloatToBuffer(vec : M3DVectorFloat) = {vec.array.position(0); vec.array.asInstanceOf[FloatBuffer]}
	implicit def M3DVectorDoubleToBuffer(vec : M3DVectorDouble) = {vec.array.position(0); vec.array.asInstanceOf[DoubleBuffer]}
  }

  // 3D points = 3D Vectors, but we need a 2D representations sometimes... (x,y) order
  final class M3DVector2f(private[this] val a : FloatBuffer = Buffers.createFloatBuffer(2)) extends M3DVectorFloat {
	@inline override def array = a
  }

  final class M3DVector2d(private[this] val a : DoubleBuffer = Buffers.createDoubleBuffer(2)) extends M3DVectorDouble {
	def this () = this(Buffers.createDoubleBuffer(2))
	@inline override def array = a
  }

  // Vector of three floats (x, y, z) Vector of three doubles (x, y, z)
  final class M3DVector3f(private[this] val a : FloatBuffer = Buffers.createFloatBuffer(3)) extends M3DVectorFloat {
	@inline override def array = a
  }

  final class M3DVector3d(private[this] val a : DoubleBuffer = Buffers.createDoubleBuffer(3)) extends M3DVectorDouble {
	@inline override def array = a
  }

  // Lesser used... Do we really need these? Yes, occasionaly we do need a trailing w component
  final class M3DVector4f(private[this] val a : FloatBuffer = Buffers.createFloatBuffer(4)) extends M3DVectorFloat {
	@inline override def array = a
  }

  final class M3DVector4d(private[this] val a : DoubleBuffer = Buffers.createDoubleBuffer(4)) extends M3DVectorDouble {
	@inline override def array = a
  }

  final class M3DVector4i(private[this] val a : IntBuffer = Buffers.createIntBuffer(4)) extends M3DVectorInt {
	@inline override def array = a
  }

  trait M3DVectorArray[T <: M3DVector[_]]

  // Creates an array of Vectors that share a common FloatBuffer
  final class M3DVector4fArray (private val buffer : FloatBuffer) extends M3DVectorArray[M3DVector4f] {
	def this(size : Int) = this(Buffers.createFloatBuffer(4 * size))
	private[this] val vectors = new Array[M3DVector4f](buffer.capacity / 4)

	for (i <- 0 until vectors.length) {
	  buffer.position(i * 4)
	  buffer.limit(i * 4 + 4)
	  vectors(i) =  new M3DVector4f(buffer.slice)
	}
	buffer.clear()

	@inline final def apply(i : Int) : M3DVector4f = vectors(i)

	def slice(length : Int, start : Int = 0) = {
	  buffer.position(start * 4)
	  buffer.limit(4 * (start + length))

	  val res = buffer.slice

	  buffer.clear()

	  res
	}
  }

  // Creates an array of Vectors that share a common FloatBuffer
  final class M3DVector3fArray (private val buffer : FloatBuffer) extends M3DVectorArray[M3DVector3f] {
	def this(size : Int) = this(Buffers.createFloatBuffer(3 * size))
	private[this] val vectors = new Array[M3DVector3f](buffer.capacity / 3)

	for (i <- 0 until vectors.length) {
	  buffer.position(i * 3)
	  buffer.limit(i * 3 + 3)
	  vectors(i) =  new M3DVector3f(buffer.slice)
	}
	buffer.clear()

	@inline final def apply(i : Int) : M3DVector3f = vectors(i)

	def slice(length : Int, start : Int = 0) = {
	  buffer.position(start * 3)
	  buffer.limit(3 * (start + length))

	  val res = buffer.slice

	  buffer.clear()

	  res
	}
  }

  // Creates an array of Vectors that share a common FloatBuffer
  final class M3DVector2fArray (private val buffer : FloatBuffer) extends M3DVectorArray[M3DVector2f] {
	def this(size : Int) = this(Buffers.createFloatBuffer(2 * size))
	private[this] val vectors = new Array[M3DVector2f](buffer.capacity / 2)

	for (i <- 0 until vectors.length) {
	  buffer.position(i * 2)
	  buffer.limit(i * 2 + 2)
	  vectors(i) =  new M3DVector2f(buffer.slice)
	}
	buffer.clear()

	@inline final def apply(i : Int) : M3DVector2f = vectors(i)

	def slice(length : Int, start : Int = 0) = {
	  buffer.position(start * 2)
	  buffer.limit(2 * (start + length))

	  val res = buffer.slice

	  buffer.clear()

	  res
	}
  }

  // Creates an array of Vectors that share a common FloatBuffer
  final class M3DMatrix44fArray (private val buffer : FloatBuffer) extends M3DVectorArray[M3DMatrix44f] {
	def this(size : Int) = this(Buffers.createFloatBuffer(2 * size))
	private[this] val matrixes = new Array[M3DMatrix44f](buffer.capacity / 16)

	for (i <- 0 until matrixes.length) {
	  buffer.position(i * 16)
	  buffer.limit(i * 16 + 16)
	  matrixes(i) =  new M3DMatrix44f(buffer.slice)
	}
	buffer.clear()

	@inline final def apply(i : Int) : M3DMatrix44f = matrixes(i)

	def slice(length : Int, start : Int = 0) = {
	  buffer.position(start * 16)
	  buffer.limit(16 * (start + length))

	  val res = buffer.slice

	  buffer.clear()

	  res
	}
  }

  object M3DVector4fArray {
	implicit def toFloatBuffer(vecArray : M3DVector4fArray) = vecArray.buffer
  }

  object M3DVector3fArray {
	implicit def toFloatBuffer(vecArray : M3DVector3fArray) = vecArray.buffer
  }

  object M3DVector2fArray {
	implicit def toFloatBuffer(vecArray : M3DVector2fArray) = vecArray.buffer
  }

  // 3x3 matrix - column major. X vector is 0, 1, 2, etc.
  //		0	3	6
  //		1	4	7
  //		2	5	8

  // A 3 x 3 matrix, column major (floats) - OpenGL Style
  final class M3DMatrix33f(private[this] val a : FloatBuffer = Buffers.createFloatBuffer(9)) extends M3DVectorFloat {
	@inline override def array = a
  }

  // A 3 x 3 matrix, column major (doubles) - OpenGL Style
  final class M3DMatrix33d(private[this] val a : DoubleBuffer = Buffers.createDoubleBuffer(9)) extends M3DVectorDouble {
	@inline override def array = a
  }

  // 4x4 matrix - column major. X vector is 0, 1, 2, etc.
  //	0	4	8	12
  //	1	5	9	13
  //	2	6	10	14
  //	3	7	11	15
  //
  // A 4 x 4 matrix, column major (floats) - OpenGL Style
  final class M3DMatrix44f(private[this] val a : FloatBuffer = Buffers.createFloatBuffer(16)) extends M3DVectorFloat {
	@inline override def array = a
  }

  // A 4 x 4 matrix, column major (doubles) - OpenGL Style
  final class M3DMatrix44d(private[this] val a : DoubleBuffer = Buffers.createDoubleBuffer(16)) extends M3DVectorDouble {
	@inline override def array = a
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Useful constants
  val M3D_PI = 3.14159265358979323846
  val M3D_2PI = 2.0 * M3D_PI
  val M3D_PI_DIV_180 = 0.017453292519943296
  val M3D_INV_PI_DIV_180 = 57.2957795130823229


  ///////////////////////////////////////////////////////////////////////////////
  // Useful shortcuts and macros
  // Radians are king... but we need a way to swap back and forth for programmers and presentation.
  // Leaving these as Macros instead of inline functions, causes constants
  // to be evaluated at compile time instead of run time, e.g. m3dDegToRad(90.0)

  // Macros not possible in Scala so inlining it is
  
  @inline def m3dDegToRad(x : Double) =	(x * M3D_PI_DIV_180)
  @inline def m3dDegToRad(x : Float) = (x * M3D_PI_DIV_180).toFloat
  @inline def m3dRadToDeg(x : Double) =	(x * M3D_INV_PI_DIV_180)
  @inline def m3dRadToDeg(x : Float) = (x * M3D_INV_PI_DIV_180).toFloat

  // Hour angles
  @inline def m3dHrToDeg(x : Double) =	((x) * (1.0 / 15.0))
  @inline def m3dHrToDeg(x : Float) =	((x) * (1.0 / 15.0)).toFloat
  @inline def m3dHrToRad(x : Double) = m3dDegToRad(m3dHrToDeg(x))
  @inline def m3dHrToRad(x : Float) = m3dDegToRad(m3dHrToDeg(x)).toFloat

  @inline def m3dDegToHr(x : Double) = ((x) * 15.0)
  @inline def m3dDegToHr(x : Float) = ((x) * 15.0).toFloat

  @inline def m3dRadToHr(x : Double) = m3dDegToHr(m3dRadToDeg(x))
  @inline def m3dRadToHr(x : Float) = m3dDegToHr(m3dRadToDeg(x))

  // Returns the same number if it is a power of
  // two. Returns a larger integer if it is not a
  // power of two. The larger integer is the next
  // highest power of two.
  // Changed to Signed Int
  @inline def m3dIsPOW2(iValue : Int) : Int = {
    var nPow2 = 1;

    while(iValue > nPow2)
	  nPow2 = (nPow2 << 1)

    return nPow2
  }


  ///////////////////////////////////////////////////////////////////////////////
  // Inline accessor functions (Macros) for people who just can't count to 3 or 4
  // Really... you should learn to count before you learn to program ;-)
  // 0 = x
  // 1 = y
  // 2 = z
  // 3 = w
  @inline def m3dGetVectorX(v : M3DVector2f) = v(0)
  @inline def m3dGetVectorY(v : M3DVector2f) = v(1)

  @inline def m3dGetVectorX(v : M3DVector3f) = v(0)
  @inline def m3dGetVectorY(v : M3DVector3f) = v(1)
  @inline def m3dGetVectorZ(v : M3DVector3f) = v(2)

  @inline def m3dGetVectorX(v : M3DVector4f) = v(0)
  @inline def m3dGetVectorY(v : M3DVector4f) = v(1)
  @inline def m3dGetVectorZ(v : M3DVector4f) = v(2)
  @inline def m3dGetVectorW(v : M3DVector4f) = v(3)

  @inline def m3dGetVectorX(v : M3DVector2d) = v(0)
  @inline def m3dGetVectorY(v : M3DVector2d) = v(1)

  @inline def m3dGetVectorX(v : M3DVector3d) = v(0)
  @inline def m3dGetVectorY(v : M3DVector3d) = v(1)
  @inline def m3dGetVectorZ(v : M3DVector3d) = v(2)

  @inline def m3dGetVectorX(v : M3DVector4d) = v(0)
  @inline def m3dGetVectorY(v : M3DVector4d) = v(1)
  @inline def m3dGetVectorZ(v : M3DVector4d) = v(2)
  @inline def m3dGetVectorW(v : M3DVector4d) = v(3)

  @inline def m3dSetVectorX(v : M3DVector2f, x : Float) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector2f, y : Float) { v(1) = y }

  @inline def m3dSetVectorX(v : M3DVector3f, x : Float) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector3f, y : Float) { v(1) = y }
  @inline def m3dSetVectorZ(v : M3DVector3f, z : Float) { v(2) = z }

  @inline def m3dSetVectorX(v : M3DVector4f, x : Float) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector4f, y : Float) { v(1) = y }
  @inline def m3dSetVectorZ(v : M3DVector4f, z : Float) { v(2) = z }
  @inline def m3dSetVectorW(v : M3DVector4f, w : Float) { v(3) = w }

  @inline def m3dSetVectorX(v : M3DVector2d, x : Double) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector2d, y : Double) { v(1) = y }

  @inline def m3dSetVectorX(v : M3DVector3d, x : Double) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector3d, y : Double) { v(1) = y }
  @inline def m3dSetVectorZ(v : M3DVector3d, z : Double) { v(2) = z }

  @inline def m3dSetVectorX(v : M3DVector4d, x : Double) { v(0) = x }
  @inline def m3dSetVectorY(v : M3DVector4d, y : Double) { v(1) = y }
  @inline def m3dSetVectorZ(v : M3DVector4d, z : Double) { v(2) = z }
  @inline def m3dSetVectorW(v : M3DVector4d, w : Double) { v(3) = w }

  ///////////////////////////////////////////////////////////////////////////////
  // Inline vector functions
  // Load Vector with (x, y, z, w).
  @inline def m3dLoadVector2(v : M3DVector2f, x : Float, y : Float) { v(0) = x; v(1) = y; }
  @inline def m3dLoadVector2(v : M3DVector2d, x : Double, y : Double) { v(0) = x; v(1) = y; }
  @inline def m3dLoadVector3(v : M3DVector3f, x : Float, y : Float, z : Float) { v(0) = x; v(1) = y; v(2) = z; }
  @inline def m3dLoadVector3(v : M3DVector3d, x : Double, y : Double, z : Double) { v(0) = x; v(1) = y; v(2) = z; }
  @inline def m3dLoadVector4(v : M3DVector4f, x : Float, y : Float, z : Float, w : Float) { v(0) = x; v(1) = y; v(2) = z; v(3) = w;}
  @inline def m3dLoadVector4(v : M3DVector4d, x : Double, y : Double, z : Double, w : Double) { v(0) = x; v(1) = y; v(2) = z; v(3) = w;}


  ////////////////////////////////////////////////////////////////////////////////
  // Copy vector src into vector dst
  @inline def	m3dCopyVector2(dst : M3DVector2f, src : M3DVector2f) { dst.copy(src) }
  @inline def	m3dCopyVector2(dst : M3DVector2d, src : M3DVector2d) { dst.copy(src) }

  @inline def	m3dCopyVector3(dst : M3DVector3f, src : M3DVector3f) { dst.copy(src) }
  @inline def	m3dCopyVector3(dst : M3DVector3d, src : M3DVector3d) { dst.copy(src) }

  @inline def	m3dCopyVector4(dst : M3DVector4f, src : M3DVector4f) { dst.copy(src) }
  @inline def	m3dCopyVector4(dst : M3DVector4d, src : M3DVector4d) { dst.copy(src) }


  ////////////////////////////////////////////////////////////////////////////////
  // Add Vectors (r, a, b) r = a + b
  @inline def m3dAddVectors2(r : M3DVector2f, a : M3DVector2f, b : M3DVector2f)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1);  }
  @inline def m3dAddVectors2(r : M3DVector2d, a : M3DVector2d, b : M3DVector2d)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1);  }

  @inline def m3dAddVectors3(r : M3DVector3f, a : M3DVector3f, b : M3DVector3f)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1); r(2) = a(2) + b(2); }
  @inline def m3dAddVectors3(r : M3DVector3d, a : M3DVector3d, b : M3DVector3d)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1); r(2) = a(2) + b(2); }

  @inline def m3dAddVectors4(r : M3DVector4f, a : M3DVector4f, b : M3DVector4f)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1); r(2) = a(2) + b(2); r(3) = a(3) + b(3); }
  @inline def m3dAddVectors4(r : M3DVector4d, a : M3DVector4d, b : M3DVector4d)
  { r(0) = a(0) + b(0);	r(1) = a(1) + b(1); r(2) = a(2) + b(2); r(3) = a(3) + b(3); }

  ////////////////////////////////////////////////////////////////////////////////
  // Subtract Vectors (r, a, b) r = a - b
  @inline def m3dSubtractVectors2(r : M3DVector2f, a : M3DVector2f, b : M3DVector2f)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1);  }
  @inline def m3dSubtractVectors2(r : M3DVector2d, a : M3DVector2d, b : M3DVector2d)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); }

  @inline def m3dSubtractVectors3(r : M3DVector3f, a : M3DVector3f, b : M3DVector3f)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }
  @inline def m3dSubtractVectors3(r : M3DVector3d, a : M3DVector3d, b : M3DVector3d)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }
  @inline def m3dSubtractVectors3(r : M3DVector3f, a : M3DVector4f, b : M3DVector4f)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }
  @inline def m3dSubtractVectors3(r : M3DVector3d, a : M3DVector4d, b : M3DVector4d)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }
  @inline def m3dSubtractVectors3(r : M3DVector4f, a : M3DVector3f, b : M3DVector3f)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }
  @inline def m3dSubtractVectors3(r : M3DVector4d, a : M3DVector3d, b : M3DVector3d)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); }

  @inline def m3dSubtractVectors4(r : M3DVector4f, a : M3DVector4f, b : M3DVector4f)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); r(3) = a(3) - b(3); }
  @inline def m3dSubtractVectors4(r : M3DVector4d, a : M3DVector4d, b : M3DVector4d)
  { r(0) = a(0) - b(0); r(1) = a(1) - b(1); r(2) = a(2) - b(2); r(3) = a(3) - b(3); }



  ///////////////////////////////////////////////////////////////////////////////////////
  // Scale Vectors (in place)
  @inline def m3dScaleVector2(v : M3DVector2f, scale : Float) { v(0) *= scale; v(1) *= scale; }
  @inline def m3dScaleVector2(v : M3DVector2d, scale : Double) { v(0) *= scale; v(1) *= scale; }

  @inline def m3dScaleVector3(v : M3DVector3f, scale : Float) { v(0) *= scale; v(1) *= scale; v(2) *= scale; }
  @inline def m3dScaleVector3(v : M3DVector3d, scale : Double) { v(0) *= scale; v(1) *= scale; v(2) *= scale; }
  @inline def m3dScaleVector3(v : M3DVector4f, scale : Float) { v(0) *= scale; v(1) *= scale; v(2) *= scale; }
  @inline def m3dScaleVector3(v : M3DVector4d, scale : Double) { v(0) *= scale; v(1) *= scale; v(2) *= scale; }

  @inline def m3dScaleVector3(v : M3DMatrix33f, index : Int, scale : Float) { v(index+0) *= scale; v(index+1) *= scale; v(index+2) *= scale; }
  @inline def m3dScaleVector3(v : M3DMatrix33d, index : Int, scale : Double) { v(index+0) *= scale; v(index+1) *= scale; v(index+2) *= scale; }
  @inline def m3dScaleVector3(v : M3DMatrix44f, index : Int, scale : Float) { v(index+0) *= scale; v(index+1) *= scale; v(index+2) *= scale; }
  @inline def m3dScaleVector3(v : M3DMatrix44d, index : Int, scale : Double) { v(index+0) *= scale; v(index+1) *= scale; v(index+2) *= scale; }

  @inline def m3dScaleVector4(v : M3DVector4f, scale : Float) { v(0) *= scale; v(1) *= scale; v(2) *= scale; v(3) *= scale; }
  @inline def m3dScaleVector4(v : M3DVector4d, scale : Double) { v(0) *= scale; v(1) *= scale; v(2) *= scale; v(3) *= scale; }


  //////////////////////////////////////////////////////////////////////////////////////
  // Cross Product
  // u x v = result
  // 3 component vectors only.
  @inline def m3dCrossProduct3(result : M3DVector3f, u : M3DVector3f, v : M3DVector3f)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }
  @inline def m3dCrossProduct3(result : M3DVector3f, u : M3DVector4f, v : M3DVector4f)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }

  @inline def m3dCrossProduct3(result : M3DVector3d, u : M3DVector3d, v : M3DVector3d)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }
  @inline def m3dCrossProduct3(result : M3DVector3d, u : M3DVector4d, v : M3DVector4d)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }
  @inline def m3dCrossProduct3(result : M3DVector4f, u : M3DVector3f, v : M3DVector3f)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }
  @inline def m3dCrossProduct3(result : M3DVector4f, u : M3DVector4f, v : M3DVector4f)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }

  @inline def m3dCrossProduct3(result : M3DVector4d, u : M3DVector3d, v : M3DVector3d)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }
  @inline def m3dCrossProduct3(result : M3DVector4d, u : M3DVector4d, v : M3DVector4d)
  {
	result(0) = u(1)*v(2) - v(1)*u(2);
	result(1) = -u(0)*v(2) + v(0)*u(2);
	result(2) = u(0)*v(1) - v(0)*u(1);
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // Dot Product, only for three component vectors
  // return u dot v
  @inline def m3dDotProduct3(u : M3DVector3f, v : M3DVector3f) : Float = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector4f, v : M3DVector3f) : Float = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector4f, v : M3DVector4f) : Float = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector3f, v : M3DVector4f) : Float = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);

  @inline def m3dDotProduct3(u : M3DVector3d, v : M3DVector3d) : Double = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector4d, v : M3DVector3d) : Double = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector4d, v : M3DVector4d) : Double = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);
  @inline def m3dDotProduct3(u : M3DVector3d, v : M3DVector4d) : Double = u(0)*v(0) + u(1)*v(1) + u(2)*v(2);

  //////////////////////////////////////////////////////////////////////////////////////
  // Angle between vectors, only for three component vectors. Angle is in radians...
  @inline def m3dGetAngleBetweenVectors3(u : M3DVector3f, v : M3DVector3f) : Float = {
    val dTemp = m3dDotProduct3(u, v)
	acos(dTemp).toFloat
  }

  @inline def m3dGetAngleBetweenVectors3(u : M3DVector3d, v : M3DVector3d) : Double = {
    val dTemp = m3dDotProduct3(u, v)
    acos(dTemp)
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // Get Square of a vectors length
  // Only for three component vectors
  @inline def m3dGetVectorLengthSquared3(u : M3DVector3f) : Float = { (u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)) }
  @inline def m3dGetVectorLengthSquared3(u : M3DVector3d) : Double = { (u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)); }
  @inline def m3dGetVectorLengthSquared3(u : M3DVector4f) : Float = { (u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)) }
  @inline def m3dGetVectorLengthSquared3(u : M3DVector4d) : Double = { (u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2)); }

  @inline def m3dGetVectorLengthSquared3(u : M3DMatrix33f, i : Int) : Float = { (u(i+0) * u(i+0)) + (u(i+1) * u(i+1)) + (u(i+2) * u(i+2)) }
  @inline def m3dGetVectorLengthSquared3(u : M3DMatrix33d, i : Int) : Double = { (u(i+0) * u(i+0)) + (u(i+1) * u(i+1)) + (u(i+2) * u(i+2)); }
  @inline def m3dGetVectorLengthSquared3(u : M3DMatrix44f, i : Int) : Float = { (u(i+0) * u(i+0)) + (u(i+1) * u(i+1)) + (u(i+2) * u(i+2)) }
  @inline def m3dGetVectorLengthSquared3(u : M3DMatrix44d, i : Int) : Double = { (u(i+0) * u(i+0)) + (u(i+1) * u(i+1)) + (u(i+2) * u(i+2)); }

  //////////////////////////////////////////////////////////////////////////////////////
  // Get lenght of vector
  // Only for three component vectors.
  @inline def m3dGetVectorLength3(u : M3DVector3f) : Float = sqrt(m3dGetVectorLengthSquared3(u)).toFloat
  @inline def m3dGetVectorLength3(u : M3DVector3d) : Double = sqrt(m3dGetVectorLengthSquared3(u));

  @inline def m3dGetVectorLength3(u : M3DVector4f) : Float = sqrt(m3dGetVectorLengthSquared3(u)).toFloat
  @inline def m3dGetVectorLength3(u : M3DVector4d) : Double = sqrt(m3dGetVectorLengthSquared3(u));

  @inline def m3dGetVectorLength3(u : M3DMatrix33f, index : Int) : Float = sqrt(m3dGetVectorLengthSquared3(u, index)).toFloat
  @inline def m3dGetVectorLength3(u : M3DMatrix33d, index : Int) : Double = sqrt(m3dGetVectorLengthSquared3(u, index));

  @inline def m3dGetVectorLength3(u : M3DMatrix44f, index : Int) : Float = sqrt(m3dGetVectorLengthSquared3(u, index)).toFloat
  @inline def m3dGetVectorLength3(u : M3DMatrix44d, index : Int) : Double = sqrt(m3dGetVectorLengthSquared3(u, index));

  //////////////////////////////////////////////////////////////////////////////////////
  // Normalize a vector
  // Scale a vector to unit length. Easy, just scale the vector by it's length
  @inline def m3dNormalizeVector3(u : M3DVector3f) { m3dScaleVector3(u, 1.0f / m3dGetVectorLength3(u)); }
  @inline def m3dNormalizeVector3(u : M3DVector3d) { m3dScaleVector3(u, 1.0 / m3dGetVectorLength3(u)); }
  @inline def m3dNormalizeVector3(u : M3DVector4f) { m3dScaleVector3(u, 1.0f / m3dGetVectorLength3(u)); }
  @inline def m3dNormalizeVector3(u : M3DVector4d) { m3dScaleVector3(u, 1.0 / m3dGetVectorLength3(u)); }

  @inline def m3dNormalizeVector3(u : M3DMatrix33f, index : Int) { m3dScaleVector3(u, index, 1.0f / m3dGetVectorLength3(u, index)); }
  @inline def m3dNormalizeVector3(u : M3DMatrix33d, index : Int) { m3dScaleVector3(u, index, 1.0 / m3dGetVectorLength3(u, index)); }
  @inline def m3dNormalizeVector3(u : M3DMatrix44f, index : Int) { m3dScaleVector3(u, index, 1.0f / m3dGetVectorLength3(u, index)); }
  @inline def m3dNormalizeVector3(u : M3DMatrix44d, index : Int) { m3dScaleVector3(u, index, 1.0 / m3dGetVectorLength3(u, index)); }


  //////////////////////////////////////////////////////////////////////////////////////
  // Get the distance between two points. The distance between two points is just
  // the magnitude of the difference between two vectors
  // Located in math.cpp
  def m3dGetDistanceSquared3(u : M3DVector3f, v : M3DVector3f) : Float = {
	val x = u(0) - v(0)
	val y = u(1) - v(1)
	val z = u(2) - v(2)

	(x*x + y*y + z*z)
  }
  def m3dGetDistanceSquared3(u : M3DVector3d, v : M3DVector3d) : Double = {
	val x = u(0) - v(0)
	val y = u(1) - v(1)
	val z = u(2) - v(2)

	(x*x + y*y + z*z)
  }

  @inline def m3dGetDistance3(u : M3DVector3d, v : M3DVector3d) : Double = sqrt(m3dGetDistanceSquared3(u, v))

  @inline def m3dGetDistance3(u : M3DVector3f, v : M3DVector3f) : Float = sqrt(m3dGetDistanceSquared3(u, v)).toFloat

  @inline def m3dGetMagnitudeSquared3(u : M3DVector3f) : Float = u(0)*u(0) + u(1)*u(1) + u(2)*u(2);
  @inline def m3dGetMagnitudeSquared3(u : M3DVector3d) : Double = u(0)*u(0) + u(1)*u(1) + u(2)*u(2);

  @inline def m3dGetMagnitude3(u : M3DVector3f) : Float = sqrt(m3dGetMagnitudeSquared3(u)).toFloat
  @inline def m3dGetMagnitude3(u : M3DVector3d) : Double = sqrt(m3dGetMagnitudeSquared3(u))



  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Matrix functions
  // Both floating point and double precision 3x3 and 4x4 matricies are supported.
  // No support is included for arbitrarily dimensioned matricies on purpose, since
  // the 3x3 and 4x4 matrix routines are the most common for the purposes of this
  // library. Matrices are column major, like OpenGL matrices.
  // Unlike the vector functions, some of these are going to have to not be inlined,
  // although many will be.

  // Copy Matrix
  // Brain-dead memcpy
  @inline def m3dCopyMatrix33(dst : M3DMatrix33f, src : M3DMatrix33f) { dst copy src }
  @inline def m3dCopyMatrix33(dst : M3DMatrix33d, src : M3DMatrix33d) { dst copy src }
  @inline def m3dCopyMatrix44(dst : M3DMatrix44f, src : M3DMatrix44f) { dst copy src }
  @inline def m3dCopyMatrix44(dst : M3DMatrix44d, src : M3DMatrix44d) { dst copy src }

  // LoadIdentity
  // Don't be fooled, this is still column major
  private val identity33f = Array(1.0f, 0.0f, 0.0f,
								  0.0f, 1.0f, 0.0f,
								  0.0f, 0.0f, 1.0f)
  private val identity33d = Array(1.0, 0.0, 0.0,
								  0.0, 1.0, 0.0,
								  0.0, 0.0, 1.0)
  private val identity44f = Array(1.0f, 0.0f, 0.0f, 0.0f,
								  0.0f, 1.0f, 0.0f, 0.0f,
								  0.0f, 0.0f, 1.0f, 0.0f,
								  0.0f, 0.0f, 0.0f, 1.0f)
  private val identity44d = Array(1.0, 0.0, 0.0, 0.0,
								  0.0, 1.0, 0.0, 0.0,
								  0.0, 0.0, 1.0, 0.0,
								  0.0, 0.0, 0.0, 1.0)

  def m3dLoadIdentity33(dst : M3DMatrix33f) = dst copy identity33f
  def m3dLoadIdentity33(dst : M3DMatrix33d) = dst copy identity33d
  def m3dLoadIdentity44(dst : M3DMatrix44f) = dst copy identity44f
  def m3dLoadIdentity44(dst : M3DMatrix44d) = dst copy identity44d

  /////////////////////////////////////////////////////////////////////////////
  // Get/Set Column.
  @inline def m3dGetMatrixColumn33(dst : M3DVector3f, src : M3DMatrix33f, column : Int) { dst.copy(0, src, (3 * column), 3); }
  @inline def m3dGetMatrixColumn33(dst : M3DVector3d, src : M3DMatrix33d, column : Int) { dst.copy(0, src, (3 * column), 3); }
  @inline def m3dSetMatrixColumn33(dst : M3DMatrix33f, src : M3DVector3f, column : Int) { dst.copy(3 * column, src, 0, 3); }
  @inline def m3dSetMatrixColumn33(dst : M3DMatrix33d, src : M3DVector3d, column : Int) { dst.copy(3 * column, src, 0, 3); }

  @inline def m3dGetMatrixColumn44(dst : M3DVector4f, src : M3DMatrix44f, column : Int) { dst.copy(0, src, (4 * column), 4); }
  @inline def m3dGetMatrixColumn44(dst : M3DVector4d, src : M3DMatrix44d, column : Int) { dst.copy(0, src, (4 * column), 4); }
  @inline def m3dSetMatrixColumn44(dst : M3DMatrix44f, src : M3DVector4f, column : Int) { dst.copy(4 * column, src, 0, 4); }
  @inline def m3dSetMatrixColumn44(dst : M3DMatrix44d, src : M3DVector4d, column : Int) { dst.copy(4 * column, src, 0, 4); }

  @inline def m3dSetMatrixColumn44(dst : M3DMatrix44f, src : M3DVector3f, column : Int) { dst.copy(4 * column, src, 0, 3); }
  @inline def m3dSetMatrixColumn44(dst : M3DMatrix44d, src : M3DVector3d, column : Int) { dst.copy(4 * column, src, 0, 3); }


///////////////////////////////////////////////////////////////////////////////
// Extract a rotation matrix from a 4x4 matrix
// Extracts the rotation matrix (3x3) from a 4x4 matrix
  @inline def m3dExtractRotationMatrix33(dst : M3DMatrix33f, src : M3DMatrix44f)
  {
	dst.copy(0, src, 0, 3); // X column
	dst.copy(3, src, 4, 3); // Y column
	dst.copy(6, src, 8, 3); // Z column
  }

// Ditto above, but for doubles
  @inline def m3dExtractRotationMatrix33(dst : M3DMatrix33d, src : M3DMatrix44d)
  {
	dst.copy(0, src, 0, 3); // X column
	dst.copy(3, src, 4, 3); // Y column
	dst.copy(6, src, 8, 3); // Z column
  }

// Inject Rotation (3x3) into a full 4x4 matrix...
  @inline def m3dInjectRotationMatrix44(dst : M3DMatrix44f, src : M3DMatrix33f)
  {
	dst.copy(0, src, 0, 4); // X column
	dst.copy(4, src, 4, 4); // Y column
	dst.copy(8, src, 8, 4); // Z column
  }

// Ditto above for doubles
  @inline def m3dInjectRotationMatrix44(dst : M3DMatrix44d, src : M3DMatrix33d)
  {
	dst.copy(0, src, 0, 4); // X column
	dst.copy(4, src, 4, 4); // Y column
	dst.copy(8, src, 8, 4); // Z column
  }

  ////////////////////////////////////////////////////////////////////////////////
  // MultMatrix
  def m3dMatrixMultiply44(product : M3DMatrix44f, a : M3DMatrix44f, b : M3DMatrix44f) {
	for (i <- 0 until 4) {
	  val ai0 = a((0<<2)+i)
	  val ai1 = a((1<<2)+i)
	  val ai2 = a((2<<2)+i)
	  val ai3 = a((3<<2)+i)
	  product((0<<2)+i) = ai0 * b((0<<2)+0) + ai1 * b((0<<2)+1) + ai2 * b((0<<2)+2) + ai3 * b((0<<2)+3);
	  product((1<<2)+i) = ai0 * b((1<<2)+0) + ai1 * b((1<<2)+1) + ai2 * b((1<<2)+2) + ai3 * b((1<<2)+3);
	  product((2<<2)+i) = ai0 * b((2<<2)+0) + ai1 * b((2<<2)+1) + ai2 * b((2<<2)+2) + ai3 * b((2<<2)+3);
	  product((3<<2)+i) = ai0 * b((3<<2)+0) + ai1 * b((3<<2)+1) + ai2 * b((3<<2)+2) + ai3 * b((3<<2)+3);
	}
  }
  def m3dMatrixMultiply44(product : M3DMatrix44d, a : M3DMatrix44d, b : M3DMatrix44d) {
	for (i <- 0 until 4) {
	  val ai0 = a((0<<2)+i)
	  val ai1 = a((1<<2)+i)
	  val ai2 = a((2<<2)+i)
	  val ai3 = a((3<<2)+i)
	  product((0<<2)+i) = ai0 * b((0<<2)+0) + ai1 * b((0<<2)+1) + ai2 * b((0<<2)+2) + ai3 * b((0<<2)+3);
	  product((1<<2)+i) = ai0 * b((1<<2)+0) + ai1 * b((1<<2)+1) + ai2 * b((1<<2)+2) + ai3 * b((1<<2)+3);
	  product((2<<2)+i) = ai0 * b((2<<2)+0) + ai1 * b((2<<2)+1) + ai2 * b((2<<2)+2) + ai3 * b((2<<2)+3);
	  product((3<<2)+i) = ai0 * b((3<<2)+0) + ai1 * b((3<<2)+1) + ai2 * b((3<<2)+2) + ai3 * b((3<<2)+3);
	}
  }

  def m3dMatrixMultiply33(product : M3DMatrix33f, a : M3DMatrix33f, b : M3DMatrix33f) {
	for (i <- 0 until 3) {
	  val ai0 = a((0*3)+i)
	  val ai1 = a((1*3)+i)
	  val ai2 = a((2*3)+i)
	  product((0*3)+i) = ai0 * b((0*3)+0) + ai1 * b((0*3)+1) + ai2 * b((0*3)+2);
	  product((1*3)+i) = ai0 * b((1*3)+0) + ai1 * b((1*3)+1) + ai2 * b((1*3)+2);
	  product((2*3)+i) = ai0 * b((2*3)+0) + ai1 * b((2*3)+1) + ai2 * b((2*3)+2);
	}
  }

  def m3dMatrixMultiply33(product : M3DMatrix33d, a : M3DMatrix33d, b : M3DMatrix33d) {
	for (i <- 0 until 3) {
	  val ai0 = a((0*3)+i)
	  val ai1 = a((1*3)+i)
	  val ai2 = a((2*3)+i)
	  product((0*3)+i) = ai0 * b((0*3)+0) + ai1 * b((0*3)+1) + ai2 * b((0*3)+2);
	  product((1*3)+i) = ai0 * b((1*3)+0) + ai1 * b((1*3)+1) + ai2 * b((1*3)+2);
	  product((2*3)+i) = ai0 * b((2*3)+0) + ai1 * b((2*3)+1) + ai2 * b((2*3)+2);
	}
  }

  // Transform - Does rotation and translation via a 4x4 matrix. Transforms
  // a point or vector.
  // By-the-way __inline means I'm asking the compiler to do a cost/benefit analysis. If
  // these are used frequently, they may not be inlined to save memory. I'm experimenting
  // with this....
  // Just transform a 3 compoment vector
  def m3dTransformVector3(vOut : M3DVector3f, v : M3DVector3f, m : M3DMatrix44f)
  {
    vOut(0) = m(0) * v(0) + m(4) * v(1) + m(8) *  v(2) + m(12);// * v(3);	// Assuming 1
    vOut(1) = m(1) * v(0) + m(5) * v(1) + m(9) *  v(2) + m(13);// * v(3);
    vOut(2) = m(2) * v(0) + m(6) * v(1) + m(10) * v(2) + m(14);// * v(3);
	//vOut(3) = m(3) * v(0) + m(7) * v(1) + m(11) * v(2) + m(15) * v(3);
  }

  // Ditto above, but for doubles
  def m3dTransformVector3(vOut : M3DVector3d, v : M3DVector3d, m : M3DMatrix44d)
  {
    vOut(0) = m(0) * v(0) + m(4) * v(1) + m(8) *  v(2) + m(12);// * v(3);
    vOut(1) = m(1) * v(0) + m(5) * v(1) + m(9) *  v(2) + m(13);// * v(3);
    vOut(2) = m(2) * v(0) + m(6) * v(1) + m(10) * v(2) + m(14);// * v(3);
	//vOut(3) = m(3) * v(0) + m(7) * v(1) + m(11) * v(2) + m(15) * v(3);
  }

  // Full four component transform
  def m3dTransformVector4(vOut : M3DVector4f, v : M3DVector4f, m : M3DMatrix44f)
  {
    vOut(0) = m(0) * v(0) + m(4) * v(1) + m(8) *  v(2) + m(12) * v(3);
    vOut(1) = m(1) * v(0) + m(5) * v(1) + m(9) *  v(2) + m(13) * v(3);
    vOut(2) = m(2) * v(0) + m(6) * v(1) + m(10) * v(2) + m(14) * v(3);
	vOut(3) = m(3) * v(0) + m(7) * v(1) + m(11) * v(2) + m(15) * v(3);
  }

  // Ditto above, but for doubles
  def m3dTransformVector4(vOut : M3DVector4d, v : M3DVector4d, m : M3DMatrix44d)
  {
    vOut(0) = m(0) * v(0) + m(4) * v(1) + m(8) *  v(2) + m(12) * v(3);
    vOut(1) = m(1) * v(0) + m(5) * v(1) + m(9) *  v(2) + m(13) * v(3);
    vOut(2) = m(2) * v(0) + m(6) * v(1) + m(10) * v(2) + m(14) * v(3);
	vOut(3) = m(3) * v(0) + m(7) * v(1) + m(11) * v(2) + m(15) * v(3);
  }



  // Just do the rotation, not the translation... this is usually done with a 3x3
  // Matrix.
  def m3dRotateVector(vOut : M3DVector3f, p : M3DVector3f, m : M3DMatrix33f)
  {
    vOut(0) = m(0) * p(0) + m(3) * p(1) + m(6) * p(2);
    vOut(1) = m(1) * p(0) + m(4) * p(1) + m(7) * p(2);
    vOut(2) = m(2) * p(0) + m(5) * p(1) + m(8) * p(2);
  }

  // Ditto above, but for doubles
  def m3dRotateVector(vOut : M3DVector3d, p : M3DVector3d, m : M3DMatrix33d)
  {
    vOut(0) = m(0) * p(0) + m(3) * p(1) + m(6) * p(2);
    vOut(1) = m(1) * p(0) + m(4) * p(1) + m(7) * p(2);
    vOut(2) = m(2) * p(0) + m(5) * p(1) + m(8) * p(2);
  }


  // Create a Scaling Matrix
  @inline def m3dScaleMatrix33(m : M3DMatrix33f, xScale : Float, yScale : Float, zScale : Float)
  { m3dLoadIdentity33(m); m(0) = xScale; m(4) = yScale; m(8) = zScale; }

  @inline def m3dScaleMatrix33(m : M3DMatrix33f, vScale : M3DVector3f)
  { m3dLoadIdentity33(m); m(0) = vScale(0); m(4) = vScale(1); m(8) = vScale(2); }

  @inline def m3dScaleMatrix33(m : M3DMatrix33d, xScale : Double, yScale : Double, zScale : Double)
  { m3dLoadIdentity33(m); m(0) = xScale; m(4) = yScale; m(8) = zScale; }

  @inline def m3dScaleMatrix33(m : M3DMatrix33d, vScale : M3DVector3d)
  { m3dLoadIdentity33(m); m(0) = vScale(0); m(4) = vScale(1); m(8) = vScale(2); }

  @inline def m3dScaleMatrix44(m : M3DMatrix44f, xScale : Float, yScale : Float, zScale : Float)
  { m3dLoadIdentity44(m); m(0) = xScale; m(5) = yScale; m(10) = zScale; }

  @inline def m3dScaleMatrix44(m : M3DMatrix44f, vScale : M3DVector3f)
  { m3dLoadIdentity44(m); m(0) = vScale(0); m(5) = vScale(1); m(10) = vScale(2); }

  @inline def m3dScaleMatrix44(m : M3DMatrix44d, xScale : Double, yScale : Double, zScale : Double)
  { m3dLoadIdentity44(m); m(0) = xScale; m(5) = yScale; m(10) = zScale; }

  @inline def m3dScaleMatrix44(m : M3DMatrix44d, vScale : M3DVector3d)
  { m3dLoadIdentity44(m); m(0) = vScale(0); m(5) = vScale(1); m(10) = vScale(2); }


  def m3dMakePerspectiveMatrix(mProjection : M3DMatrix44f, fFov : Float, fAspect : Float, zMin : Float, zMax : Float) {
	m3dLoadIdentity44(mProjection); // Fastest way to get most valid values already in place

    val yMax = zMin * tan(fFov * 0.5f).toFloat;
    val yMin = -yMax;
	val xMin = yMin * fAspect;
    val xMax = -xMin;

	mProjection(0) = (2.0f * zMin) / (xMax - xMin);
	mProjection(5) = (2.0f * zMin) / (yMax - yMin);
	mProjection(8) = (xMax + xMin) / (xMax - xMin);
	mProjection(9) = (yMax + yMin) / (yMax - yMin);
	mProjection(10) = -((zMax + zMin) / (zMax - zMin));
	mProjection(11) = -1.0f;
	mProjection(14) = -((2.0f * (zMax*zMin))/(zMax - zMin));
	mProjection(15) = 0.0f;
  }
  def m3dMakeOrthographicMatrix(mProjection : M3DMatrix44f, xMin : Float, xMax : Float, yMin : Float, yMax : Float, zMin : Float, zMax : Float) {
	m3dLoadIdentity44(mProjection);

	mProjection(0) = 2.0f / (xMax - xMin);
	mProjection(5) = 2.0f / (yMax - yMin);
	mProjection(10) = -2.0f / (zMax - zMin);
	mProjection(12) = -((xMax + xMin)/(xMax - xMin));
	mProjection(13) = -((yMax + yMin)/(yMax - yMin));
	mProjection(14) = -((zMax + zMin)/(zMax - zMin));
	mProjection(15) = 1.0f;
  }

  // Create a Rotation matrix
  // Implemented in math3d.cpp
  def m3dRotationMatrix33(m : M3DMatrix33f, angle : Float, _x : Float, _y : Float, _z : Float) {
	val s = sin(angle).toFloat
	val c = cos(angle).toFloat
	val mag = sqrt(_x*_x + _y*_y + _z*_z).toFloat

	// Identity matrix
	if (mag == 0.0f) {
	  m3dLoadIdentity33(m);
	  return;
	}

	// Rotation matrix is normalized
	val x = _x / mag;
	val y = _y / mag;
	val z = _z / mag;

	val xx = x * x;
	val yy = y * y;
	val zz = z * z;
	val xy = x * y;
	val yz = y * z;
	val zx = z * x;
	val xs = x * s;
	val ys = y * s;
	val zs = z * s;
	val one_c = 1.0f - c;

	m((0*3)+0) = (one_c * xx) + c;
	m((1*3)+0) = (one_c * xy) - zs;
	m((2*3)+0) = (one_c * zx) + ys;

	m((0*3)+1) = (one_c * xy) + zs;
	m((1*3)+1) = (one_c * yy) + c;
	m((2*3)+1) = (one_c * yz) - xs;

	m((0*3)+2) = (one_c * zx) - ys;
	m((1*3)+2) = (one_c * yz) + xs;
	m((2*3)+2) = (one_c * zz) + c;
  }
  def m3dRotationMatrix33(m : M3DMatrix33d, angle : Double, _x : Double, _y : Double, _z : Double) {
	val s = sin(angle)
	val c = cos(angle)
	val mag = sqrt(_x*_x + _y*_y + _z*_z)

	// Identity matrix
	if (mag == 0.0f) {
	  m3dLoadIdentity33(m);
	  return;
	}

	// Rotation matrix is normalized
	val x = _x / mag;
	val y = _y / mag;
	val z = _z / mag;

	val xx = x * x;
	val yy = y * y;
	val zz = z * z;
	val xy = x * y;
	val yz = y * z;
	val zx = z * x;
	val xs = x * s;
	val ys = y * s;
	val zs = z * s;
	val one_c = 1.0 - c;

	m((0*3)+0) = (one_c * xx) + c;
	m((1*3)+0) = (one_c * xy) - zs;
	m((2*3)+0) = (one_c * zx) + ys;

	m((0*3)+1) = (one_c * xy) + zs;
	m((1*3)+1) = (one_c * yy) + c;
	m((2*3)+1) = (one_c * yz) - xs;

	m((0*3)+2) = (one_c * zx) - ys;
	m((1*3)+2) = (one_c * yz) + xs;
	m((2*3)+2) = (one_c * zz) + c;
  }

  def m3dRotationMatrix44(m : M3DMatrix44f, angle : Float, _x : Float, _y : Float, _z : Float) {
	val s = sin(angle).toFloat
	val c = cos(angle).toFloat
	val mag = sqrt(_x*_x + _y*_y + _z*_z).toFloat

	// Identity matrix
	if (mag == 0.0f) {
	  m3dLoadIdentity44(m);
	  return;
	}

	// Rotation matrix is normalized
	val x = _x / mag;
	val y = _y / mag;
	val z = _z / mag;

	val xx = x * x;
	val yy = y * y;
	val zz = z * z;
	val xy = x * y;
	val yz = y * z;
	val zx = z * x;
	val xs = x * s;
	val ys = y * s;
	val zs = z * s;
	val one_c = 1.0f - c;

	m((0*4)+0) = (one_c * xx) + c;
	m((1*4)+0) = (one_c * xy) - zs;
	m((2*4)+0) = (one_c * zx) + ys;
	m((3*4)+0) = 0.0f

	m((0*4)+1) = (one_c * xy) + zs;
	m((1*4)+1) = (one_c * yy) + c;
	m((2*4)+1) = (one_c * yz) - xs;
	m((3*4)+1) = 0.0f

	m((0*4)+2) = (one_c * zx) - ys;
	m((1*4)+2) = (one_c * yz) + xs;
	m((2*4)+2) = (one_c * zz) + c;
	m((3*4)+2) = 0.0f

	m((0*4)+3) = 0.0f
	m((1*4)+3) = 0.0f
	m((2*4)+3) = 0.0f
	m((3*4)+3) = 1.0f
  }
  def m3dRotationMatrix44(m : M3DMatrix44d, angle : Double, _x : Double, _y : Double, _z : Double) {
	val s = sin(angle)
	val c = cos(angle)
	val mag = sqrt(_x*_x + _y*_y + _z*_z)

	// Identity matrix
	if (mag == 0.0f) {
	  m3dLoadIdentity44(m);
	  return;
	}

	// Rotation matrix is normalized
	val x = _x / mag;
	val y = _y / mag;
	val z = _z / mag;

	val xx = x * x;
	val yy = y * y;
	val zz = z * z;
	val xy = x * y;
	val yz = y * z;
	val zx = z * x;
	val xs = x * s;
	val ys = y * s;
	val zs = z * s;
	val one_c = 1.0 - c;

	m((0*4)+0) = (one_c * xx) + c;
	m((1*4)+0) = (one_c * xy) - zs;
	m((2*4)+0) = (one_c * zx) + ys;
	m((3*4)+0) = 0.0

	m((0*4)+1) = (one_c * xy) + zs;
	m((1*4)+1) = (one_c * yy) + c;
	m((2*4)+1) = (one_c * yz) - xs;
	m((3*4)+1) = 0.0

	m((0*4)+2) = (one_c * zx) - ys;
	m((1*4)+2) = (one_c * yz) + xs;
	m((2*4)+2) = (one_c * zz) + c;
	m((3*4)+2) = 0.0

	m((0*4)+3) = 0.0
	m((1*4)+3) = 0.0
	m((2*4)+3) = 0.0
	m((3*4)+3) = 1.0
  }

  ////////////////////////////////////////////////////////////////////////////
  /// This function is not exported by library, just for this modules use only
  // 3x3 determinant
  def DetIJ(m : M3DMatrix44f, i : Int, j : Int) : Float = {
    val mat = Array.ofDim[Float](3, 3)

    var x = 0
	var ii = 0
    while (ii < 4) {
	  if (ii != i) {
		var y = 0
		var jj = 0
		while (jj < 4) {
		  if (jj != j) {
			mat(x)(y) = m((ii*4)+jj)
			y += 1
		  }
		  jj += 1
		}
		x += 1
	  }
	  ii += 1
	}

    var ret =  mat(0)(0)*(mat(1)(1)*mat(2)(2)-mat(2)(1)*mat(1)(2))
    ret -= mat(0)(1)*(mat(1)(0)*mat(2)(2)-mat(2)(0)*mat(1)(2))
    ret += mat(0)(2)*(mat(1)(0)*mat(2)(1)-mat(2)(0)*mat(1)(1))

    ret
  }
  ////////////////////////////////////////////////////////////////////////////
  /// This function is not exported by library, just for this modules use only
  // 3x3 determinant
  def DetIJ(m : M3DMatrix44d, i : Int, j : Int) : Double = {
    val mat = Array.ofDim[Double](3, 3)

    var x = 0
	var ii = 0
    while (ii < 4) {
	  if (ii != i) {
		var y = 0
		var jj = 0
		while (jj < 4) {
		  if (jj != j) {
			mat(x)(y) = m((ii*4)+jj)
			y += 1
		  }
		  jj += 1
		}
		x += 1
	  }
	  ii += 1
	}

    var ret =  mat(0)(0)*(mat(1)(1)*mat(2)(2)-mat(2)(1)*mat(1)(2))
    ret -= mat(0)(1)*(mat(1)(0)*mat(2)(2)-mat(2)(0)*mat(1)(2))
    ret += mat(0)(2)*(mat(1)(0)*mat(2)(1)-mat(2)(0)*mat(1)(1))

    ret
  }

  // Create a Translation matrix. Only 4x4 matrices have translation components
  @inline def m3dTranslationMatrix44(m : M3DMatrix44f, x : Float, y : Float, z : Float)
  { m3dLoadIdentity44(m); m(12) = x; m(13) = y; m(14) = z; }

  @inline def m3dTranslationMatrix44(m : M3DMatrix44d, x : Double, y : Double, z : Double)
  { m3dLoadIdentity44(m); m(12) = x; m(13) = y; m(14) = z; }

  def m3dInvertMatrix44(mInverse : M3DMatrix44f, m : M3DMatrix44f){
    // calculate 4x4 determinant
    var det = 0.0f;
	var i = 0
    while (i < 4) {
	  det = det + (if ((i & 0x1.toInt) != 0) { -m(i) * DetIJ(m, 0, i) } else { m(i) * DetIJ(m, 0, i) })
	  i += 1
	}
    det = 1.0f / det;

    // calculate inverse
	i = 0
    while (i < 4) {
	  var j = 0;
	  while (j < 4) {
		val detij = DetIJ(m, j, i);
		mInverse((i*4)+j) = if (((i+j) & 0x1) != 0) (-detij * det) else (detij * det)
		j += 1
	  }
	  i += 1
    }
  }
  def m3dInvertMatrix44(mInverse : M3DMatrix44d, m : M3DMatrix44d) {
	// calculate 4x4 determinant
    var det = 0.0;
	var i = 0
    while (i < 4) {
	  det = det + (if ((i & 0x1.toInt) != 0) { -m(i) * DetIJ(m, 0, i) } else { m(i) * DetIJ(m, 0, i) })
	  i += 1
	}
    det = 1.0f / det;

    // calculate inverse
	i = 0
    while (i < 4) {
	  var j = 0;
	  while (j < 4) {
		val detij = DetIJ(m, j, i);
		mInverse((i*4)+j) = if (((i+j) & 0x1) != 0) (-detij * det) else (detij *det)
		j += 1
	  }
	  i += 1
    }
  }

  ///////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////
  // Other Miscellaneous functions

  // Find a normal from three points
  // Implemented in math3d.cpp
  ///////////////////////////////////////////////////////////////////////////////
  // Calculates the normal of a triangle specified by the three points
  // p1, p2, and p3. Each pointer points to an array of three floats. The
  // triangle is assumed to be wound counter clockwise.
  private[this] val v1f = new M3DVector4f
  private[this] val v2f = new M3DVector4f
  private[this] val v3f = new M3DVector3f
  def m3dFindNormal(result : M3DVector3f, point1 : M3DVector3f, point2 : M3DVector3f, point3 : M3DVector3f) {
	// Calculate two vectors from the three points. Assumes counter clockwise
	// winding!
	v1f(0) = point1(0) - point2(0);
	v1f(1) = point1(1) - point2(1);
	v1f(2) = point1(2) - point2(2);

	v2f(0) = point2(0) - point3(0);
	v2f(1) = point2(1) - point3(1);
	v2f(2) = point2(2) - point3(2);

	// Take the cross product of the two vectors to get
	// the normal vector.
	m3dCrossProduct3(result, v1f, v2f);
  }

  private[this] val v1d = new M3DVector4d
  private[this] val v2d = new M3DVector4d
  private[this] val v3d = new M3DVector3d
  def m3dFindNormal(result : M3DVector3d, point1 : M3DVector3d, point2 : M3DVector3d, point3 : M3DVector3d) {

	// Calculate two vectors from the three points. Assumes counter clockwise
	// winding!
	v1d(0) = point1(0) - point2(0);
	v1d(1) = point1(1) - point2(1);
	v1d(2) = point1(2) - point2(2);

	v2d(0) = point2(0) - point3(0);
	v2d(1) = point2(1) - point3(1);
	v2d(2) = point2(2) - point3(2);

	// Take the cross product of the two vectors to get
	// the normal vector.
	m3dCrossProduct3(result, v1d, v2d);
  }


  // Calculates the signed distance of a point to a plane
  @inline def m3dGetDistanceToPlane(point : M3DVector3f, plane : M3DVector4f) : Float =
  { point(0)*plane(0) + point(1)*plane(1) + point(2)*plane(2) + plane(3); }

  @inline def m3dGetDistanceToPlane(point : M3DVector3d, plane : M3DVector4d) : Double =
  { point(0)*plane(0) + point(1)*plane(1) + point(2)*plane(2) + plane(3); }


  // Get plane equation from three points
  def m3dGetPlaneEquation(planeEq : M3DVector4f, p1 : M3DVector3f, p2 : M3DVector3f, p3 : M3DVector3f) {
	// Get two vectors... do the cross product

    // V1 = p3 - p1
    v1f(0) = p3(0) - p1(0);
    v1f(1) = p3(1) - p1(1);
    v1f(2) = p3(2) - p1(2);

    // V2 = P2 - p1
    v2f(0) = p2(0) - p1(0);
    v2f(1) = p2(1) - p1(1);
    v2f(2) = p2(2) - p1(2);

    // Unit normal to plane - Not sure which is the best way here
    m3dCrossProduct3(planeEq, v1f, v2f);
    m3dNormalizeVector3(planeEq);

    // Back substitute to get D
    planeEq(3) = -(planeEq(0) * p3(0) + planeEq(1) * p3(1) + planeEq(2) * p3(2));
  }

  def m3dGetPlaneEquation(planeEq : M3DVector4f, p1 : M3DVector4f, p2 : M3DVector4f, p3 : M3DVector4f) {
	// Get two vectors... do the cross product

    // V1 = p3 - p1
    v1f(0) = p3(0) - p1(0);
    v1f(1) = p3(1) - p1(1);
    v1f(2) = p3(2) - p1(2);

    // V2 = P2 - p1
    v2f(0) = p2(0) - p1(0);
    v2f(1) = p2(1) - p1(1);
    v2f(2) = p2(2) - p1(2);

    // Unit normal to plane - Not sure which is the best way here
    m3dCrossProduct3(planeEq, v1f, v2f);
    m3dNormalizeVector3(planeEq);

    // Back substitute to get D
    planeEq(3) = -(planeEq(0) * p3(0) + planeEq(1) * p3(1) + planeEq(2) * p3(2));
  }
  
  def m3dGetPlaneEquation(planeEq : M3DVector4d, p1 : M3DVector3d, p2 : M3DVector3d, p3 : M3DVector3d) {
	// Get two vectors... do the cross product

    // V1 = p3 - p1
    v1d(0) = p3(0) - p1(0);
    v1d(1) = p3(1) - p1(1);
    v1d(2) = p3(2) - p1(2);

    // V2 = P2 - p1
    v2d(0) = p2(0) - p1(0);
    v2d(1) = p2(1) - p1(1);
    v2d(2) = p2(2) - p1(2);

    // Unit normal to plane - Not sure which is the best way here
    m3dCrossProduct3(planeEq, v1d, v2d);
    m3dNormalizeVector3(planeEq);

    // Back substitute to get D
    planeEq(3) = -(planeEq(0) * p3(0) + planeEq(1) * p3(1) + planeEq(2) * p3(2));
  }

  def m3dGetPlaneEquation(planeEq : M3DVector4d, p1 : M3DVector4d, p2 : M3DVector4d, p3 : M3DVector4d) {
	// Get two vectors... do the cross product

    // V1 = p3 - p1
    v1d(0) = p3(0) - p1(0);
    v1d(1) = p3(1) - p1(1);
    v1d(2) = p3(2) - p1(2);

    // V2 = P2 - p1
    v2d(0) = p2(0) - p1(0);
    v2d(1) = p2(1) - p1(1);
    v2d(2) = p2(2) - p1(2);

    // Unit normal to plane - Not sure which is the best way here
    m3dCrossProduct3(planeEq, v1d, v2d);
    m3dNormalizeVector3(planeEq);

    // Back substitute to get D
    planeEq(3) = -(planeEq(0) * p3(0) + planeEq(1) * p3(1) + planeEq(2) * p3(2));
  }

  // Determine if a ray intersects a sphere
  // Return value is < 0 if the ray does not intersect
  // Return value is 0.0 if ray is tangent
  // Positive value is distance to the intersection point
  def m3dRaySphereTest(point : M3DVector3f, ray : M3DVector3f, sphereCenter : M3DVector3f, sphereRadius : Float) : Float = {
	//m3dNormalizeVector(ray);	// Make sure ray is unit length

	val rayToCenter = v1f
	rayToCenter(0) =  sphereCenter(0) - point(0);
	rayToCenter(1) =  sphereCenter(1) - point(1);
	rayToCenter(2) =  sphereCenter(2) - point(2);

	// Project rayToCenter on ray to test
	val a = m3dDotProduct3(rayToCenter, ray);

	// Distance to center of sphere
	val distance2 = m3dDotProduct3(rayToCenter, rayToCenter);	// Or length


	var dRet = (sphereRadius * sphereRadius) - distance2 + (a*a);

	if(dRet > 0.0f)			// Return distance to intersection
	  dRet = a - sqrt(dRet).toFloat;

	return dRet;
  }
  def m3dRaySphereTest(point : M3DVector3d, ray : M3DVector3d, sphereCenter : M3DVector3d, sphereRadius : Double) : Double = {
	//m3dNormalizeVector(ray);	// Make sure ray is unit length

	val rayToCenter = v1d;	// Ray to center of sphere
	rayToCenter(0) =  sphereCenter(0) - point(0);
	rayToCenter(1) =  sphereCenter(1) - point(1);
	rayToCenter(2) =  sphereCenter(2) - point(2);

	// Project rayToCenter on ray to test
	val a = m3dDotProduct3(rayToCenter, ray);

	// Distance to center of sphere
	val distance2 = m3dDotProduct3(rayToCenter, rayToCenter);	// Or length


	var dRet = (sphereRadius * sphereRadius) - distance2 + (a*a);

	if(dRet > 0.0)			// Return distance to intersection
	  dRet = a - scala.math.sqrt(dRet);

	return dRet;
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  // Faster (and one shortcut) replacements for gluProject
  ///////////////////////////////////////////////////////////////////////////////////////
  // Get Window coordinates, discard Z...
  def m3dProjectXY( vPointOut : M3DVector2f, mModelView : M3DMatrix44f, mProjection : M3DMatrix44f, iViewPort : M3DVector4i, vPointIn : M3DVector3f) {
	val vBack = v1f
	val vForth = v2f

	vBack.copy(vPointIn)
	vBack(3) = 1.0f;

    m3dTransformVector4(vForth, vBack, mModelView);
    m3dTransformVector4(vBack, vForth, mProjection);

    if(!m3dCloseEnough(vBack(3), 0.0f, 0.000001f)) {
	  val div = 1.0f / vBack(3);
	  vBack(0) *= div;
	  vBack(1) *= div;
	  //vBack(2) *= div;
	}

    vPointOut(0) = iViewPort(0).toFloat + (1.0f + vBack(0).toFloat) * iViewPort(2).toFloat / 2.0f;
    vPointOut(1) = iViewPort(1).toFloat + (1.0f + vBack(1).toFloat) * iViewPort(3).toFloat / 2.0f;

	// This was put in for Grand Tour... I think it's right.
	// .... please report any bugs
	if(iViewPort(0) != 0)     // Cast to float is expensive... avoid if posssible
	  vPointOut(0) -= iViewPort(0).toFloat

	if(iViewPort(1) != 0)
	  vPointOut(1) -= iViewPort(1).toFloat
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////
  // Get window coordinates, we also want Z....
  def m3dProjectXYZ(vPointOut : M3DVector3f, mModelView : M3DMatrix44f, mProjection : M3DMatrix44f, iViewPort : M3DVector4i, vPointIn : M3DVector3f) {
	val vBack = v1f
	val vForth = v2f

	vBack.copy(vPointIn)
	vBack(3) = 1.0f;

    m3dTransformVector4(vForth, vBack, mModelView);
    m3dTransformVector4(vBack, vForth, mProjection);

    if(!m3dCloseEnough(vBack(3), 0.0f, 0.000001f)) {
	  val div = 1.0f / vBack(3);
	  vBack(0) *= div;
	  vBack(1) *= div;
	  vBack(2) *= div;
	}

    vPointOut(0) = iViewPort(0).toFloat+(1.0f+vBack(0).toFloat)*iViewPort(2).toFloat/2.0f;
    vPointOut(1) = iViewPort(1).toFloat+(1.0f+vBack(1).toFloat)*iViewPort(3).toFloat/2.0f;

	if(iViewPort(0) != 0)     // Cast to float is expensive... avoid if posssible
	  vPointOut(0) -= iViewPort(0).toFloat;

	if(iViewPort(1) != 0)
	  vPointOut(1) -= iViewPort(1).toFloat;

 	vPointOut(2) = vBack(2);
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////
  // This function does a three dimensional Catmull-Rom curve interpolation. Pass four points, and a
  // floating point number between 0.0 and 1.0. The curve is interpolated between the middle two points.
  // Coded by RSW
  def m3dCatmullRom(vOut : M3DVector3f, vP0 : M3DVector3f, vP1 : M3DVector3f, vP2 : M3DVector3f, vP3 : M3DVector3f, t : Float) {
	val t2 = t * t;
    val t3 = t2 * t;

    // X
    vOut(0) = 0.5f * ( ( 2.0f * vP1(0)) +
					  (-vP0(0) + vP2(0)) * t +
					  (2.0f * vP0(0) - 5.0f *vP1(0) + 4.0f * vP2(0) - vP3(0)) * t2 +
					  (-vP0(0) + 3.0f*vP1(0) - 3.0f *vP2(0) + vP3(0)) * t3);
    // Y
    vOut(1) = 0.5f * ( ( 2.0f * vP1(1)) +
					  (-vP0(1) + vP2(1)) * t +
					  (2.0f * vP0(1) - 5.0f *vP1(1) + 4.0f * vP2(1) - vP3(1)) * t2 +
					  (-vP0(1) + 3.0f*vP1(1) - 3.0f *vP2(1) + vP3(1)) * t3);

    // Z
    vOut(2) = 0.5f * ( ( 2.0f * vP1(2)) +
					  (-vP0(2) + vP2(2)) * t +
					  (2.0f * vP0(2) - 5.0f *vP1(2) + 4.0f * vP2(2) - vP3(2)) * t2 +
					  (-vP0(2) + 3.0f*vP1(2) - 3.0f *vP2(2) + vP3(2)) * t3);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // This function does a three dimensional Catmull-Rom curve interpolation. Pass four points, and a
  // floating point number between 0.0 and 1.0. The curve is interpolated between the middle two points.
  // Coded by RSW
  def m3dCatmullRom(vOut : M3DVector3d, vP0 : M3DVector3d, vP1 : M3DVector3d, vP2 : M3DVector3d, vP3 : M3DVector3d, t : Double) {
	val t2 = t * t;
    val t3 = t2 * t;

    // X
    vOut(0) = 0.5f * ( ( 2.0f * vP1(0)) +
					  (-vP0(0) + vP2(0)) * t +
					  (2.0f * vP0(0) - 5.0f *vP1(0) + 4.0f * vP2(0) - vP3(0)) * t2 +
					  (-vP0(0) + 3.0f*vP1(0) - 3.0f *vP2(0) + vP3(0)) * t3);
    // Y
    vOut(1) = 0.5f * ( ( 2.0f * vP1(1)) +
					  (-vP0(1) + vP2(1)) * t +
					  (2.0f * vP0(1) - 5.0f *vP1(1) + 4.0f * vP2(1) - vP3(1)) * t2 +
					  (-vP0(1) + 3.0f*vP1(1) - 3.0f *vP2(1) + vP3(1)) * t3);

    // Z
    vOut(2) = 0.5f * ( ( 2.0f * vP1(2)) +
					  (-vP0(2) + vP2(2)) * t +
					  (2.0f * vP0(2) - 5.0f *vP1(2) + 4.0f * vP2(2) - vP3(2)) * t2 +
					  (-vP0(2) + 3.0f*vP1(2) - 3.0f *vP2(2) + vP3(2)) * t3);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////
  // Compare floats and doubles...
  @inline def m3dCloseEnough(fCandidate : Float, fCompare : Float, fEpsilon : Float) : Boolean =
  {
    abs(fCandidate - fCompare) < fEpsilon
  }

  @inline def m3dCloseEnough(dCandidate : Double, dCompare : Double, dEpsilon : Double) : Boolean =
  {

	abs(dCandidate - dCompare) < dEpsilon
  }

  ////////////////////////////////////////////////////////////////////////////
  // Used for normal mapping. Finds the tangent bases for a triangle...
  // Only a floating point implementation is provided. This has no practical use as doubles.
  def m3dCalculateTangentBasis(vTangent : M3DVector3f, vTriangle : Tuple3[M3DVector3f, M3DVector3f, M3DVector3f], vTexCoords : Tuple3[M3DVector2f, M3DVector2f, M3DVector2f], N : M3DVector3f) {
	val dv2v1 = v1f
	val dv3v1 = v2f
    
    m3dSubtractVectors3(dv2v1, vTriangle._2, vTriangle._1);
    m3dSubtractVectors3(dv3v1, vTriangle._3, vTriangle._1);

    val dc2c1t = vTexCoords._2(0) - vTexCoords._1(0);
    val dc2c1b = vTexCoords._2(1) - vTexCoords._1(1);
    val dc3c1t = vTexCoords._3(0) - vTexCoords._1(0);
    val dc3c1b = vTexCoords._3(1) - vTexCoords._1(1);

    val M = 1.0f / ((dc2c1t * dc3c1b) - (dc3c1t * dc2c1b))

    m3dScaleVector3(dv2v1, dc3c1b);
    m3dScaleVector3(dv3v1, dc2c1b);

    m3dSubtractVectors3(vTangent, dv2v1, dv3v1);
    m3dScaleVector3(vTangent, M);  // This potentially changes the direction of the vector
    m3dNormalizeVector3(vTangent);

    val B = v3f
    m3dCrossProduct3(B, N, vTangent);
    m3dCrossProduct3(vTangent, B, N);
    m3dNormalizeVector3(vTangent);
  }

  ////////////////////////////////////////////////////////////////////////////
  // Smoothly step between 0 and 1 between edge1 and edge 2
  def m3dSmoothStep(edge1 : Double, edge2 : Double, x : Double) : Double = {
    var t = (x - edge1) / (edge2 - edge1);
    if(t > 1.0)
	  t = 1.0;
	else if(t < 0.0)
	  t = 0.0f;

    t * t * ( 3.0 - 2.0 * t);
  }
  def m3dSmoothStep(edge1 : Float, edge2 : Float, x : Float) : Float = {
	var t = (x - edge1) / (edge2 - edge1);
    if(t > 1.0f)
	  t = 1.0f;
    else if(t < 0.0)
	  t = 0.0f;

    return t * t * ( 3.0f - 2.0f * t);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Creae a projection to "squish" an object into the plane.
  // Use m3dGetPlaneEquationf(planeEq, point1, point2, point3);
  // to get a plane equation.
  def m3dMakePlanarShadowMatrix(proj : M3DMatrix44d, planeEq : M3DVector4d, vLightPos : M3DVector3d) {
	// These just make the code below easier to read. They will be
	// removed by the optimizer.
	val a = planeEq(0);
	val b = planeEq(1);
	val c = planeEq(2);
	val d = planeEq(3);

	val dx = -vLightPos(0);
	val dy = -vLightPos(1);
	val dz = -vLightPos(2);

	// Now build the projection matrix
	proj(0) = b * dy + c * dz;
	proj(1) = -a * dy;
	proj(2) = -a * dz;
	proj(3) = 0.0;

	proj(4) = -b * dx;
	proj(5) = a * dx + c * dz;
	proj(6) = -b * dz;
	proj(7) = 0.0;

	proj(8) = -c * dx;
	proj(9) = -c * dy;
	proj(10) = a * dx + b * dy;
	proj(11) = 0.0;

	proj(12) = -d * dx;
	proj(13) = -d * dy;
	proj(14) = -d * dz;
	proj(15) = a * dx + b * dy + c * dz;
	// Shadow matrix ready
  }
  def m3dMakePlanarShadowMatrix(proj : M3DMatrix44f, planeEq : M3DVector4f, vLightPos : M3DVector3f) {
	// These just make the code below easier to read. They will be
	// removed by the optimizer.
	val a = planeEq(0);
	val b = planeEq(1);
	val c = planeEq(2);
	val d = planeEq(3);

	val dx = -vLightPos(0);
	val dy = -vLightPos(1);
	val dz = -vLightPos(2);

	// Now build the projection matrix
	proj(0) = b * dy + c * dz;
	proj(1) = -a * dy;
	proj(2) = -a * dz;
	proj(3) = 0.0f;

	proj(4) = -b * dx;
	proj(5) = a * dx + c * dz;
	proj(6) = -b * dz;
	proj(7) = 0.0f;

	proj(8) = -c * dx;
	proj(9) = -c * dy;
	proj(10) = a * dx + b * dy;
	proj(11) = 0.0f;

	proj(12) = -d * dx;
	proj(13) = -d * dy;
	proj(14) = -d * dz;
	proj(15) = a * dx + b * dy + c * dz;
	// Shadow matrix ready
  }
  def m3dMakePlanarShadowMatrix(proj : M3DMatrix44f, planeEq : M3DVector4f, vLightPos : M3DVector4f) {
	// These just make the code below easier to read. They will be
	// removed by the optimizer.
	val a = planeEq(0);
	val b = planeEq(1);
	val c = planeEq(2);
	val d = planeEq(3);

	val dx = -vLightPos(0);
	val dy = -vLightPos(1);
	val dz = -vLightPos(2);

	// Now build the projection matrix
	proj(0) = b * dy + c * dz;
	proj(1) = -a * dy;
	proj(2) = -a * dz;
	proj(3) = 0.0f;

	proj(4) = -b * dx;
	proj(5) = a * dx + c * dz;
	proj(6) = -b * dz;
	proj(7) = 0.0f;

	proj(8) = -c * dx;
	proj(9) = -c * dy;
	proj(10) = a * dx + b * dy;
	proj(11) = 0.0f;

	proj(12) = -d * dx;
	proj(13) = -d * dy;
	proj(14) = -d * dz;
	proj(15) = a * dx + b * dy + c * dz;
	// Shadow matrix ready
  }

  /////////////////////////////////////////////////////////////////////////////
  // Closest point on a ray to another point in space
  def m3dClosestPointOnRay(vPointOnRay : M3DVector3d, vRayOrigin : M3DVector3d, vUnitRayDir : M3DVector3d, vPointInSpace : M3DVector3d) : Double = {
	val v = v1d
	m3dSubtractVectors3(v, vPointInSpace, vRayOrigin);

	val t = m3dDotProduct3(vUnitRayDir, v);
	
	// This is the point on the ray
	vPointOnRay(0) = vRayOrigin(0) + (t * vUnitRayDir(0));
	vPointOnRay(1) = vRayOrigin(1) + (t * vUnitRayDir(1));
	vPointOnRay(2) = vRayOrigin(2) + (t * vUnitRayDir(2));

	return m3dGetDistanceSquared3(vPointOnRay, vPointInSpace);
  }

  def m3dClosestPointOnRay(vPointOnRay : M3DVector3f, vRayOrigin : M3DVector3f, vUnitRayDir : M3DVector3f, vPointInSpace : M3DVector3f) : Float = {
	val v = v1f
	m3dSubtractVectors3(v, vPointInSpace, vRayOrigin);

	val t = m3dDotProduct3(vUnitRayDir, v);

	// This is the point on the ray
	vPointOnRay(0) = vRayOrigin(0) + (t * vUnitRayDir(0));
	vPointOnRay(1) = vRayOrigin(1) + (t * vUnitRayDir(1));
	vPointOnRay(2) = vRayOrigin(2) + (t * vUnitRayDir(2));

	return m3dGetDistanceSquared3(vPointOnRay, vPointInSpace);
  }

}
