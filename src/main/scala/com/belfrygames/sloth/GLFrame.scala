package com.belfrygames.sloth

import com.belfrygames.sloth._
import com.belfrygames.sloth.GLTools._
import com.belfrygames.sloth.Math3D._
import com.belfrygames.sloth.glut._
import com.belfrygames.sloth.glut.Internal._

import org.lwjgl.opengl.GL11._

// The GLFrame (OrthonormalFrame) class. Possibly the most useful little piece of 3D graphics
// code for OpenGL immersive environments.
// Richard S. Wright Jr.
class GLFrame {
  // Default position and orientation. At the origin, looking
  // down the positive Z axis (right handed coordinate system).
  protected var vOrigin = new M3DVector3f
  // Where am I?
  protected var vForward = new M3DVector3f
  // Where am I going?
  protected var vUp = new M3DVector3f // Which way is up?

  // At origin
  vOrigin(0) = 0.0f;
  vOrigin(1) = 0.0f;
  vOrigin(2) = 0.0f;

  // Up is up (+Y)
  vUp(0) = 0.0f;
  vUp(1) = 1.0f;
  vUp(2) = 0.0f;

  // Forward is -Z (default OpenGL)
  vForward(0) = 0.0f;
  vForward(1) = 0.0f;
  vForward(2) = -1.0f;

  /////////////////////////////////////////////////////////////
  // Set Location
  @inline def SetOrigin(vPoint: M3DVector3f) {
    m3dCopyVector3(vOrigin, vPoint);
  }

  @inline def SetOrigin(x: Float, y: Float, z: Float) {
    vOrigin(0) = x;
    vOrigin(1) = y;
    vOrigin(2) = z;
  }

  @inline def GetOrigin(vPoint: M3DVector3f) {
    m3dCopyVector3(vPoint, vOrigin);
  }

  @inline def GetOriginX() = vOrigin(0)

  @inline def GetOriginY() = vOrigin(1)

  @inline def GetOriginZ() = vOrigin(2)

  /////////////////////////////////////////////////////////////
  // Set Forward Direction
  @inline def SetForwardVector(vDirection: M3DVector3f) {
    m3dCopyVector3(vForward, vDirection);
  }

  @inline def SetForwardVector(x: Float, y: Float, z: Float) {
    vForward(0) = x; vForward(1) = y; vForward(2) = z;
  }

  @inline def GetForwardVector(vVector: M3DVector3f) {
    m3dCopyVector3(vVector, vForward);
  }

  /////////////////////////////////////////////////////////////
  // Set Up Direction
  @inline def SetUpVector(vDirection: M3DVector3f) {
    m3dCopyVector3(vUp, vDirection);
  }

  @inline def SetUpVector(x: Float, y: Float, z: Float) {
    vUp(0) = x; vUp(1) = y; vUp(2) = z;
  }

  @inline def GetUpVector(vVector: M3DVector3f) {
    m3dCopyVector3(vVector, vUp);
  }


  /////////////////////////////////////////////////////////////
  // Get Axes
  @inline def GetZAxis(vVector: M3DVector3f) {
    GetForwardVector(vVector);
  }

  @inline def GetYAxis(vVector: M3DVector3f) {
    GetUpVector(vVector);
  }

  @inline def GetXAxis(vVector: M3DVector3f) {
    m3dCrossProduct3(vVector, vUp, vForward);
  }


  /////////////////////////////////////////////////////////////
  // Translate along orthonormal axis... world or local
  @inline def TranslateWorld(x: Float, y: Float, z: Float) {
    vOrigin(0) += x; vOrigin(1) += y; vOrigin(2) += z;
  }

  @inline def TranslateLocal(x: Float, y: Float, z: Float) {
    MoveForward(z); MoveUp(y); MoveRight(x);
  }


  /////////////////////////////////////////////////////////////
  // Move Forward (along Z axis)
  @inline def MoveForward(fDelta: Float) {
    // Move along direction of front direction
    vOrigin(0) += vForward(0) * fDelta;
    vOrigin(1) += vForward(1) * fDelta;
    vOrigin(2) += vForward(2) * fDelta;
  }

  // Move along Y axis
  @inline def MoveUp(fDelta: Float) {
    // Move along direction of up direction
    vOrigin(0) += vUp(0) * fDelta;
    vOrigin(1) += vUp(1) * fDelta;
    vOrigin(2) += vUp(2) * fDelta;
  }

  // Move along X axis
  private[this] val vTemp = new M3DVector3f;

  @inline def MoveRight(fDelta: Float) {
    // Move along direction of right vector
    val vCross = vTemp
    m3dCrossProduct3(vCross, vUp, vForward);

    vOrigin(0) += vCross(0) * fDelta;
    vOrigin(1) += vCross(1) * fDelta;
    vOrigin(2) += vCross(2) * fDelta;
  }


  ///////////////////////////////////////////////////////////////////////
  // Just assemble the matrix
  def GetMatrix(matrix: M3DMatrix44f, bRotationOnly: Boolean = false) {
    // Calculate the right side (x) vector, drop it right into the matrix
    val vXAxis = vTemp
    m3dCrossProduct3(vXAxis, vUp, vForward);

    // Set matrix column does not fill in the fourth value...
    m3dSetMatrixColumn44(matrix, vXAxis, 0);
    matrix(3) = 0.0f;

    // Y Column
    m3dSetMatrixColumn44(matrix, vUp, 1);
    matrix(7) = 0.0f;

    // Z Column
    m3dSetMatrixColumn44(matrix, vForward, 2);
    matrix(11) = 0.0f;

    // Translation (already done)
    if (bRotationOnly) {
      matrix(12) = 0.0f;
      matrix(13) = 0.0f;
      matrix(14) = 0.0f;
    }
    else
      m3dSetMatrixColumn44(matrix, vOrigin, 3);

    matrix(15) = 1.0f;
  }


  ////////////////////////////////////////////////////////////////////////
  // Assemble the camera matrix
  private[this] val vTemp2 = new M3DVector3f
  private[this] val mTemp = new M3DMatrix44f
  private[this] val mTemp2 = new M3DMatrix44f

  def GetCameraMatrix(m: M3DMatrix44f, bRotationOnly: Boolean = false) {
    val x = vTemp
    val z = vTemp2

    // Make rotation matrix
    // Z vector is reversed
    z(0) = -vForward(0);
    z(1) = -vForward(1);
    z(2) = -vForward(2);

    // X vector = Y cross Z
    m3dCrossProduct3(x, vUp, z);

    // Matrix has no translation information and is
    // transposed.... (rows instead of columns)
    m(0 * 4 + 0) = x(0);
    m(1 * 4 + 0) = x(1);
    m(2 * 4 + 0) = x(2);
    m(3 * 4 + 0) = 0.0f;
    m(0 * 4 + 1) = vUp(0);
    m(1 * 4 + 1) = vUp(1);
    m(2 * 4 + 1) = vUp(2);
    m(3 * 4 + 1) = 0.0f;
    m(0 * 4 + 2) = z(0);
    m(1 * 4 + 2) = z(1);
    m(2 * 4 + 2) = z(2);
    m(3 * 4 + 2) = 0.0f;
    m(0 * 4 + 3) = 0.0f;
    m(1 * 4 + 3) = 0.0f;
    m(2 * 4 + 3) = 0.0f;
    m(3 * 4 + 3) = 1.0f;


    if (bRotationOnly)
      return;

    // Apply translation too
    val trans = mTemp
    val M = mTemp2
    m3dTranslationMatrix44(trans, -vOrigin(0), -vOrigin(1), -vOrigin(2));

    m3dMatrixMultiply44(M, m, trans);

    // Copy result back into m
    m copy M
  }


  // Rotate around local Y
  def RotateLocalY(fAngle: Float) {
    val rotMat = mTemp

    // Just Rotate around the up vector
    // Create a rotation matrix around my Up (Y) vector
    m3dRotationMatrix44(rotMat, fAngle,
      vUp(0), vUp(1), vUp(2));

    val newVect = vTemp

    // Rotate forward pointing vector (@inlined 3x3 transform)
    newVect(0) = rotMat(0) * vForward(0) + rotMat(4) * vForward(1) + rotMat(8) * vForward(2);
    newVect(1) = rotMat(1) * vForward(0) + rotMat(5) * vForward(1) + rotMat(9) * vForward(2);
    newVect(2) = rotMat(2) * vForward(0) + rotMat(6) * vForward(1) + rotMat(10) * vForward(2);
    m3dCopyVector3(vForward, newVect);
  }


  // Rotate around local Z
  def RotateLocalZ(fAngle: Float) {
    val rotMat = mTemp

    // Only the up vector needs to be rotated
    m3dRotationMatrix44(rotMat, fAngle,
      vForward(0), vForward(1), vForward(2));

    val newVect = vTemp
    newVect(0) = rotMat(0) * vUp(0) + rotMat(4) * vUp(1) + rotMat(8) * vUp(2);
    newVect(1) = rotMat(1) * vUp(0) + rotMat(5) * vUp(1) + rotMat(9) * vUp(2);
    newVect(2) = rotMat(2) * vUp(0) + rotMat(6) * vUp(1) + rotMat(10) * vUp(2);
    m3dCopyVector3(vUp, newVect);
  }

  private[this] val mTemp3 = new M3DMatrix33f;

  def RotateLocalX(fAngle: Float) {
    val rotMat = mTemp3
    val localX = vTemp
    val rotVec = vTemp2

    // Get the local X axis
    m3dCrossProduct3(localX, vUp, vForward);

    // Make a Rotation Matrix
    m3dRotationMatrix33(rotMat, fAngle, localX(0), localX(1), localX(2));

    // Rotate Y, and Z
    m3dRotateVector(rotVec, vUp, rotMat);
    m3dCopyVector3(vUp, rotVec);

    m3dRotateVector(rotVec, vForward, rotMat);
    m3dCopyVector3(vForward, rotVec);
  }


  // Reset axes to make sure they are orthonormal. This should be called on occasion
  // if the matrix is long-lived and frequently transformed.
  def Normalize() {
    val vCross = vTemp

    // Calculate cross product of up and forward vectors
    m3dCrossProduct3(vCross, vUp, vForward);

    // Use result to recalculate forward vector
    m3dCrossProduct3(vForward, vCross, vUp);

    // Also check for unit length...
    m3dNormalizeVector3(vUp);
    m3dNormalizeVector3(vForward);
  }


  // Rotate in world coordinates...
  def RotateWorld(fAngle: Float, x: Float, y: Float, z: Float) {
    val rotMat = mTemp

    // Create the Rotation matrix
    m3dRotationMatrix44(rotMat, fAngle, x, y, z);

    val newVect = vTemp

    // Transform the up axis (@inlined 3x3 rotation)
    newVect(0) = rotMat(0) * vUp(0) + rotMat(4) * vUp(1) + rotMat(8) * vUp(2);
    newVect(1) = rotMat(1) * vUp(0) + rotMat(5) * vUp(1) + rotMat(9) * vUp(2);
    newVect(2) = rotMat(2) * vUp(0) + rotMat(6) * vUp(1) + rotMat(10) * vUp(2);
    m3dCopyVector3(vUp, newVect);

    // Transform the forward axis
    newVect(0) = rotMat(0) * vForward(0) + rotMat(4) * vForward(1) + rotMat(8) * vForward(2);
    newVect(1) = rotMat(1) * vForward(0) + rotMat(5) * vForward(1) + rotMat(9) * vForward(2);
    newVect(2) = rotMat(2) * vForward(0) + rotMat(6) * vForward(1) + rotMat(10) * vForward(2);
    m3dCopyVector3(vForward, newVect);
  }


  // Rotate around a local axis
  def RotateLocal(fAngle: Float, x: Float, y: Float, z: Float) {
    val vWorldVect = vTemp
    val vLocalVect = vTemp2
    m3dLoadVector3(vLocalVect, x, y, z);

    LocalToWorld(vLocalVect, vWorldVect, true);
    RotateWorld(fAngle, vWorldVect(0), vWorldVect(1), vWorldVect(2));
  }


  // Convert Coordinate Systems
  // This is pretty much, do the transformation represented by the rotation
  // and position on the point
  // Is it better to stick to the convention that the destination always comes
  // first, or use the conventions that "sounds" like the function...
  def LocalToWorld(vLocal: M3DVector3f, vWorld: M3DVector3f, bRotOnly: Boolean = false) {
    // Create the rotation matrix based on the vectors
    val rotMat = mTemp

    GetMatrix(rotMat, true);

    // Do the rotation (@inline it, and remove 4th column...)
    vWorld(0) = rotMat(0) * vLocal(0) + rotMat(4) * vLocal(1) + rotMat(8) * vLocal(2);
    vWorld(1) = rotMat(1) * vLocal(0) + rotMat(5) * vLocal(1) + rotMat(9) * vLocal(2);
    vWorld(2) = rotMat(2) * vLocal(0) + rotMat(6) * vLocal(1) + rotMat(10) * vLocal(2);

    // Translate the point
    if (!bRotOnly) {
      vWorld(0) += vOrigin(0);
      vWorld(1) += vOrigin(1);
      vWorld(2) += vOrigin(2);
    }
  }

  // Change world coordinates into "local" coordinates
  def WorldToLocal(vWorld: M3DVector3f, vLocal: M3DVector3f) {
    ////////////////////////////////////////////////
    // Translate the origin
    val vNewWorld = vTemp
    vNewWorld(0) = vWorld(0) - vOrigin(0);
    vNewWorld(1) = vWorld(1) - vOrigin(1);
    vNewWorld(2) = vWorld(2) - vOrigin(2);

    // Create the rotation matrix based on the vectors
    val rotMat = mTemp
    val invMat = mTemp2
    GetMatrix(rotMat, true);

    // Do the rotation based on inverted matrix
    m3dInvertMatrix44(invMat, rotMat);

    vLocal(0) = invMat(0) * vNewWorld(0) + invMat(4) * vNewWorld(1) + invMat(8) * vNewWorld(2);
    vLocal(1) = invMat(1) * vNewWorld(0) + invMat(5) * vNewWorld(1) + invMat(9) * vNewWorld(2);
    vLocal(2) = invMat(2) * vNewWorld(0) + invMat(6) * vNewWorld(1) + invMat(10) * vNewWorld(2);
  }

  /////////////////////////////////////////////////////////////////////////////
  // Transform a point by frame matrix
  def TransformPoint(vPointSrc: M3DVector3f, vPointDst: M3DVector3f) {
    val m = mTemp
    GetMatrix(m, false); // Rotate and translate
    vPointDst(0) = m(0) * vPointSrc(0) + m(4) * vPointSrc(1) + m(8) * vPointSrc(2) + m(12); // * v(3);
    vPointDst(1) = m(1) * vPointSrc(0) + m(5) * vPointSrc(1) + m(9) * vPointSrc(2) + m(13); // * v(3);
    vPointDst(2) = m(2) * vPointSrc(0) + m(6) * vPointSrc(1) + m(10) * vPointSrc(2) + m(14); // * v(3);
  }

  ////////////////////////////////////////////////////////////////////////////
  // Rotate a vector by frame matrix
  def RotateVector(vVectorSrc: M3DVector3f, vVectorDst: M3DVector3f) {
    val m = mTemp
    GetMatrix(m, true); // Rotate only

    vVectorDst(0) = m(0) * vVectorSrc(0) + m(4) * vVectorSrc(1) + m(8) * vVectorSrc(2);
    vVectorDst(1) = m(1) * vVectorSrc(0) + m(5) * vVectorSrc(1) + m(9) * vVectorSrc(2);
    vVectorDst(2) = m(2) * vVectorSrc(0) + m(6) * vVectorSrc(1) + m(10) * vVectorSrc(2);
  }
}

