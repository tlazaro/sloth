package com.belfrygames.sloth.exp.memory.unsafe

import scala.math._

object UnsafeMath3D {
  ///////////////////////////////////////////////////////////////////////////////
  // Useful constants
  val M3D_PI = 3.14159265358979323846
  val M3D_2PI = 2.0 * M3D_PI
  val M3D_PI_DIV_180 = 0.017453292519943296
  val M3D_INV_PI_DIV_180 = 57.2957795130823229

  @inline def m3dDegToRad(x: Double) = (x * M3D_PI_DIV_180)

  @inline def m3dDegToRad(x: Float) = (x * M3D_PI_DIV_180).toFloat

  private val identity33f = M3DMatrix33f(1.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 1.0f)

  private val identity44f = M3DMatrix44f(1.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 1.0f)

  @inline def m3dLoadIdentity33(dst: M3DMatrix33f) = dst copy identity33f

  @inline def m3dLoadIdentity44(dst: M3DMatrix44f) = dst copy identity44f

  @inline def m3dCopyMatrix44(dst: M3DMatrix44f, src: M3DMatrix44f): Unit = dst copy src

  @inline def m3dLoadVector3(v: M3DVector3f, x: Float, y: Float, z: Float) {
    v(0) = x;
    v(1) = y;
    v(2) = z;
  }

  def m3dMatrixMultiply44(product: M3DMatrix44f, a: M3DMatrix44f, b: M3DMatrix44f) {
    for (i <- 0 until 4) {
      val ai0 = a((0 << 2) + i)
      val ai1 = a((1 << 2) + i)
      val ai2 = a((2 << 2) + i)
      val ai3 = a((3 << 2) + i)
      product((0 << 2) + i) = ai0 * b((0 << 2) + 0) + ai1 * b((0 << 2) + 1) + ai2 * b((0 << 2) + 2) + ai3 * b((0 << 2) + 3);
      product((1 << 2) + i) = ai0 * b((1 << 2) + 0) + ai1 * b((1 << 2) + 1) + ai2 * b((1 << 2) + 2) + ai3 * b((1 << 2) + 3);
      product((2 << 2) + i) = ai0 * b((2 << 2) + 0) + ai1 * b((2 << 2) + 1) + ai2 * b((2 << 2) + 2) + ai3 * b((2 << 2) + 3);
      product((3 << 2) + i) = ai0 * b((3 << 2) + 0) + ai1 * b((3 << 2) + 1) + ai2 * b((3 << 2) + 2) + ai3 * b((3 << 2) + 3);
    }
  }

  @inline def m3dScaleMatrix44(m: M3DMatrix44f, xScale: Float, yScale: Float, zScale: Float) {
    m3dLoadIdentity44(m);
    m(0) = xScale;
    m(5) = yScale;
    m(10) = zScale;
  }

  @inline def m3dScaleMatrix44(m: M3DMatrix44f, vScale: M3DVector3f) {
    m3dLoadIdentity44(m);
    m(0) = vScale(0);
    m(5) = vScale(1);
    m(10) = vScale(2);
  }

  @inline def m3dTranslationMatrix44(m: M3DMatrix44f, x: Float, y: Float, z: Float) {
    m3dLoadIdentity44(m);
    m(12) = x;
    m(13) = y;
    m(14) = z;
  }

  @inline def m3dSetMatrixColumn44(dst: M3DMatrix44f, src: M3DVector3f, column: Int) {
    dst.copy(4 * column, src.address, 0, 3);
  }

//  @inline def m3dSetMatrixColumn44(dst: M3DMatrix44f, src: M3DVector4f, column: Int) {
//    dst.copy(4 * column, src.address, 0, 4);
//  }

  @inline def m3dCopyVector3(dst: M3DVector3f, src: M3DVector3f): Unit = dst copy src

  def m3dRotationMatrix44(m: M3DMatrix44f, angle: Float, _x: Float, _y: Float, _z: Float) {
    val s = sin(angle).toFloat
    val c = cos(angle).toFloat
    val mag = sqrt(_x * _x + _y * _y + _z * _z).toFloat

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

    m((0 * 4) + 0) = (one_c * xx) + c;
    m((1 * 4) + 0) = (one_c * xy) - zs;
    m((2 * 4) + 0) = (one_c * zx) + ys;
    m((3 * 4) + 0) = 0.0f

    m((0 * 4) + 1) = (one_c * xy) + zs;
    m((1 * 4) + 1) = (one_c * yy) + c;
    m((2 * 4) + 1) = (one_c * yz) - xs;
    m((3 * 4) + 1) = 0.0f

    m((0 * 4) + 2) = (one_c * zx) - ys;
    m((1 * 4) + 2) = (one_c * yz) + xs;
    m((2 * 4) + 2) = (one_c * zz) + c;
    m((3 * 4) + 2) = 0.0f

    m((0 * 4) + 3) = 0.0f
    m((1 * 4) + 3) = 0.0f
    m((2 * 4) + 3) = 0.0f
    m((3 * 4) + 3) = 1.0f
  }

  // Create a Rotation matrix
  // Implemented in math3d.cpp
  def m3dRotationMatrix33(m: M3DMatrix33f, angle: Float, _x: Float, _y: Float, _z: Float) {
    val s = sin(angle).toFloat
    val c = cos(angle).toFloat
    val mag = sqrt(_x * _x + _y * _y + _z * _z).toFloat

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

    m((0 * 3) + 0) = (one_c * xx) + c;
    m((1 * 3) + 0) = (one_c * xy) - zs;
    m((2 * 3) + 0) = (one_c * zx) + ys;

    m((0 * 3) + 1) = (one_c * xy) + zs;
    m((1 * 3) + 1) = (one_c * yy) + c;
    m((2 * 3) + 1) = (one_c * yz) - xs;

    m((0 * 3) + 2) = (one_c * zx) - ys;
    m((1 * 3) + 2) = (one_c * yz) + xs;
    m((2 * 3) + 2) = (one_c * zz) + c;
  }

  ////////////////////////////////////////////////////////////////////////////
  /// This function is not exported by library, just for this modules use only
  // 3x3 determinant
  def DetIJ(m: M3DMatrix44f, i: Int, j: Int): Float = {
    val mat = Array.ofDim[Float](3, 3)

    var x = 0
    var ii = 0
    while (ii < 4) {
      if (ii != i) {
        var y = 0
        var jj = 0
        while (jj < 4) {
          if (jj != j) {
            mat(x)(y) = m((ii * 4) + jj)
            y += 1
          }
          jj += 1
        }
        x += 1
      }
      ii += 1
    }

    var ret = mat(0)(0) * (mat(1)(1) * mat(2)(2) - mat(2)(1) * mat(1)(2))
    ret -= mat(0)(1) * (mat(1)(0) * mat(2)(2) - mat(2)(0) * mat(1)(2))
    ret += mat(0)(2) * (mat(1)(0) * mat(2)(1) - mat(2)(0) * mat(1)(1))

    ret
  }

  def m3dInvertMatrix44(mInverse: M3DMatrix44f, m: M3DMatrix44f) {
    // calculate 4x4 determinant
    var det = 0.0f;
    var i = 0
    while (i < 4) {
      det = det + (if ((i & 0x1.toInt) != 0) {
        -m(i) * DetIJ(m, 0, i)
      } else {
        m(i) * DetIJ(m, 0, i)
      })
      i += 1
    }
    det = 1.0f / det;

    // calculate inverse
    i = 0
    while (i < 4) {
      var j = 0;
      while (j < 4) {
        val detij = DetIJ(m, j, i);
        mInverse((i * 4) + j) = if (((i + j) & 0x1) != 0) (-detij * det) else (detij * det)
        j += 1
      }
      i += 1
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////
  // Cross Product
  // u x v = result
  // 3 component vectors only.
  @inline def m3dCrossProduct3(result: M3DVector3f, u: M3DVector3f, v: M3DVector3f) {
    result(0) = u(1) * v(2) - v(1) * u(2);
    result(1) = -u(0) * v(2) + v(0) * u(2);
    result(2) = u(0) * v(1) - v(0) * u(1);
  }

  // Just do the rotation, not the translation... this is usually done with a 3x3
  // Matrix.
  def m3dRotateVector(vOut: M3DVector3f, p: M3DVector3f, m: M3DMatrix33f) {
    vOut(0) = m(0) * p(0) + m(3) * p(1) + m(6) * p(2);
    vOut(1) = m(1) * p(0) + m(4) * p(1) + m(7) * p(2);
    vOut(2) = m(2) * p(0) + m(5) * p(1) + m(8) * p(2);
  }

  @inline def m3dGetVectorLengthSquared3(u: M3DVector3f): Float = {
    (u(0) * u(0)) + (u(1) * u(1)) + (u(2) * u(2))
  }

  @inline def m3dGetVectorLengthSquared3(u: M3DMatrix33f, i: Int): Float = {
    (u(i + 0) * u(i + 0)) + (u(i + 1) * u(i + 1)) + (u(i + 2) * u(i + 2))
  }

  @inline def m3dGetVectorLength3(u: M3DVector3f): Float = sqrt(m3dGetVectorLengthSquared3(u)).toFloat

  @inline def m3dNormalizeVector3(u: M3DVector3f) {
    m3dScaleVector3(u, 1.0f / m3dGetVectorLength3(u));
  }

  @inline def m3dGetVectorLength3(u: M3DMatrix33f, index: Int): Float = sqrt(m3dGetVectorLengthSquared3(u, index)).toFloat

  @inline def m3dNormalizeVector3(u: M3DMatrix33f, index: Int) {
    m3dScaleVector3(u, index, 1.0f / m3dGetVectorLength3(u, index));
  }

  @inline def m3dScaleVector3(v: M3DMatrix33f, index: Int, scale: Float) {
    v(index + 0) *= scale; v(index + 1) *= scale; v(index + 2) *= scale;
  }

  @inline def m3dScaleVector3(v: M3DVector3f, scale: Float) {
    v(0) *= scale;
    v(1) *= scale;
    v(2) *= scale;
  }

  // Full four component transform
  def m3dTransformVector4(vOut: M3DVector4f, v: M3DVector4f, m: M3DMatrix44f) {
    vOut(0) = m(0) * v(0) + m(4) * v(1) + m(8) * v(2) + m(12) * v(3);
    vOut(1) = m(1) * v(0) + m(5) * v(1) + m(9) * v(2) + m(13) * v(3);
    vOut(2) = m(2) * v(0) + m(6) * v(1) + m(10) * v(2) + m(14) * v(3);
    vOut(3) = m(3) * v(0) + m(7) * v(1) + m(11) * v(2) + m(15) * v(3);
  }

  // Calculates the signed distance of a point to a plane
  @inline def m3dGetDistanceToPlane(point: M3DVector3f, plane: M3DVector4f): Float = {
    point(0) * plane(0) + point(1) * plane(1) + point(2) * plane(2) + plane(3);
  }

  // Calculates the normal of a triangle specified by the three points
  // p1, p2, and p3. Each pointer points to an array of three floats. The
  // triangle is assumed to be wound counter clockwise.
  private[this] val v1f = M3DVector4f()
  private[this] val v2f = M3DVector4f()
  private[this] val v3f = M3DVector3f()

  def m3dGetPlaneEquation(planeEq: M3DVector4f, p1: M3DVector4f, p2: M3DVector4f, p3: M3DVector4f) {
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
    m3dCrossProduct3(new M3DVector3f(planeEq.address), new M3DVector3f(v1f.address), new M3DVector3f(v2f.address));
    m3dNormalizeVector3(new M3DVector3f(planeEq.address));

    // Back substitute to get D
    planeEq(3) = -(planeEq(0) * p3(0) + planeEq(1) * p3(1) + planeEq(2) * p3(2));
  }

  def m3dMakeOrthographicMatrix(mProjection: M3DMatrix44f, xMin: Float, xMax: Float, yMin: Float, yMax: Float, zMin: Float, zMax: Float) {
    m3dLoadIdentity44(mProjection);

    mProjection(0) = 2.0f / (xMax - xMin);
    mProjection(5) = 2.0f / (yMax - yMin);
    mProjection(10) = -2.0f / (zMax - zMin);
    mProjection(12) = -((xMax + xMin) / (xMax - xMin));
    mProjection(13) = -((yMax + yMin) / (yMax - yMin));
    mProjection(14) = -((zMax + zMin) / (zMax - zMin));
    mProjection(15) = 1.0f;
  }

  ///////////////////////////////////////////////////////////////////////////////
  // Extract a rotation matrix from a 4x4 matrix
  // Extracts the rotation matrix (3x3) from a 4x4 matrix
  @inline def m3dExtractRotationMatrix33(dst: M3DMatrix33f, src: M3DMatrix44f) {
    dst.copy(0, src.address, 0, 3); // X column
    dst.copy(3, src.address, 4, 3); // Y column
    dst.copy(6, src.address, 8, 3); // Z column
  }
}
