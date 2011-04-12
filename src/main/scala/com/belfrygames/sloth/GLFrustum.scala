package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D._

object GLFrustum {
  def get () = {
	val f = new GLFrustum
	f.SetOrthographic(-1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f)
	f
  }

  def get (fFov : Float, fAspect : Float, fNear : Float, fFar : Float) = {
	val f = new GLFrustum
	f.SetPerspective(fFov, fAspect, fNear, fFar)
	f
  }

  def get (xMin : Float, xMax : Float, yMin : Float, yMax : Float, zMin : Float, zMax : Float) = {
	val f = new GLFrustum
	f.SetOrthographic(xMin, xMax, yMin, yMax, zMin, zMax)
	f
  }
}

class GLFrustum private() {
   // The projection matrix for this frustum
  protected val projMatrix = new M3DMatrix44f;

  // Untransformed corners of the frustum
  protected val nearUL = new M3DVector4f
  protected val nearLL = new M3DVector4f
  protected val nearUR = new M3DVector4f
  protected val nearLR = new M3DVector4f

  protected val farUL = new M3DVector4f
  protected val farLL = new M3DVector4f
  protected val farUR = new M3DVector4f
  protected val farLR = new M3DVector4f

  // Transformed corners of Frustum
  protected val nearULT = new M3DVector4f
  protected val nearLLT = new M3DVector4f
  protected val nearURT = new M3DVector4f
  protected val nearLRT = new M3DVector4f

  protected val farULT = new M3DVector4f
  protected val farLLT = new M3DVector4f
  protected val farURT = new M3DVector4f
  protected val farLRT = new M3DVector4f

  // Base and Transformed plane equations
  protected val nearPlane = new M3DVector4f
  protected val farPlane = new M3DVector4f
  protected val leftPlane = new M3DVector4f
  protected val rightPlane = new M3DVector4f

  protected val topPlane = new M3DVector4f
  protected val bottomPlane = new M3DVector4f
  
  // Get the projection matrix for this guy
  def GetProjectionMatrix() = projMatrix

  // Calculates the corners of the Frustum and sets the projection matrix.
  // Orthographics Matrix Projection
  def SetOrthographic(xMin : Float, xMax : Float, yMin : Float, yMax : Float, zMin : Float, zMax : Float) {
	m3dMakeOrthographicMatrix(projMatrix, xMin, xMax, yMin, yMax, zMin, zMax);
	projMatrix(15) = 1.0f;


	// Fill in values for untransformed Frustum corners
	// Near Upper Left
	nearUL(0) = xMin; nearUL(1) = yMax; nearUL(2) = zMin; nearUL(3) = 1.0f;

	// Near Lower Left
	nearLL(0) = xMin; nearLL(1) = yMin; nearLL(2) = zMin; nearLL(3) = 1.0f;

	// Near Upper Right
	nearUR(0) = xMax; nearUR(1) = yMax; nearUR(2) = zMin; nearUR(3) = 1.0f;

	// Near Lower Right
	nearLR(0) = xMax; nearLR(1) = yMin; nearLR(2) = zMin; nearLR(3) = 1.0f;

	// Far Upper Left
	farUL(0) = xMin; farUL(1) = yMax; farUL(2) = zMax; farUL(3) = 1.0f;

	// Far Lower Left
	farLL(0) = xMin; farLL(1) = yMin; farLL(2) = zMax; farLL(3) = 1.0f;

	// Far Upper Right
	farUR(0) = xMax; farUR(1) = yMax; farUR(2) = zMax; farUR(3) = 1.0f;

	// Far Lower Right
	farLR(0) = xMax; farLR(1) = yMin; farLR(2) = zMax; farLR(3) = 1.0f;
  }


  // Calculates the corners of the Frustum and sets the projection matrix.
  // Perspective Matrix Projection
  def SetPerspective(fFov : Float, fAspect : Float, fNear : Float, fFar : Float) {
	// Do the Math for the near clipping plane
	val ymax = fNear * scala.math.tan( fFov * M3D_PI / 360.0 ).toFloat
	val ymin = -ymax;
	val xmin = ymin * fAspect;
	val xmax = -xmin;

	// Construct the projection matrix
	m3dLoadIdentity44(projMatrix);
	projMatrix(0) = (2.0f * fNear)/(xmax - xmin);
	projMatrix(5) = (2.0f * fNear)/(ymax - ymin);
	projMatrix(8) = (xmax + xmin) / (xmax - xmin);
	projMatrix(9) = (ymax + ymin) / (ymax - ymin);
	projMatrix(10) = -((fFar + fNear)/(fFar - fNear));
	projMatrix(11) = -1.0f;
	projMatrix(14) = -((2.0f * fFar * fNear)/(fFar - fNear));
	projMatrix(15) = 0.0f;

	// Do the Math for the far clipping plane
	val yFmax = fFar * scala.math.tan(fFov * M3D_PI / 360.0).toFloat
	val yFmin = -yFmax;
	val xFmin = yFmin * fAspect;
	val xFmax = -xFmin;

	// Fill in values for untransformed Frustum corners
	// Near Upper Left
	nearUL(0) = xmin; nearUL(1) = ymax; nearUL(2) = -fNear; nearUL(3) = 1.0f;

	// Near Lower Left
	nearLL(0) = xmin; nearLL(1) = ymin; nearLL(2) = -fNear; nearLL(3) = 1.0f;

	// Near Upper Right
	nearUR(0) = xmax; nearUR(1) = ymax; nearUR(2) = -fNear; nearUR(3) = 1.0f;

	// Near Lower Right
	nearLR(0) = xmax; nearLR(1) = ymin; nearLR(2) = -fNear; nearLR(3) = 1.0f;

	// Far Upper Left
	farUL(0) = xFmin; farUL(1) = yFmax; farUL(2) = -fFar; farUL(3) = 1.0f;

	// Far Lower Left
	farLL(0) = xFmin; farLL(1) = yFmin; farLL(2) = -fFar; farLL(3) = 1.0f;

	// Far Upper Right
	farUR(0) = xFmax; farUR(1) = yFmax; farUR(2) = -fFar; farUR(3) = 1.0f;

	// Far Lower Right
	farLR(0) = xFmax; farLR(1) = yFmin; farLR(2) = -fFar; farLR(3) = 1.0f;
  }


  // Builds a transformation matrix and transforms the corners of the Frustum,
  // then derives the plane equations
  def Transform(Camera : GLFrame) {
	// Workspace
	val rotMat = new M3DMatrix44f
	val vForward = new M3DVector3f
	val vUp = new M3DVector3f
	val vCross = new M3DVector3f
	val vOrigin = new M3DVector3f

	///////////////////////////////////////////////////////////////////
	// Create the transformation matrix. This was the trickiest part
	// for me. The default view from OpenGL is down the negative Z
	// axis. However, building a transformation axis from these
	// directional vectors points the frustum the wrong direction. So
	// You must reverse them here, or build the initial frustum
	// backwards - which to do is purely a matter of taste. I chose to
	// compensate here to allow better operability with some of my other
	// legacy code and projects. RSW
	Camera.GetForwardVector(vForward);
	vForward(0) = -vForward(0);
	vForward(1) = -vForward(1);
	vForward(2) = -vForward(2);

	Camera.GetUpVector(vUp);
	Camera.GetOrigin(vOrigin);

	// Calculate the right side (x) vector
	m3dCrossProduct3(vCross, vUp, vForward);

	// The Matrix
	// X Column
	// copy(offset : Int, other : M3DVector[T], otherOffset : Int, length : Int)
	rotMat.copy(0, vCross, 0, 3)
	rotMat(3) = 0.0f;

	// Y Column
	rotMat.copy(4, vUp, 0, 3)
	rotMat(7) = 0.0f;

	// Z Column
	rotMat.copy(8, vForward, 0, 3)
	rotMat(11) = 0.0f;

	// Translation
	rotMat(12) = vOrigin(0);
	rotMat(13) = vOrigin(1);
	rotMat(14) = vOrigin(2);
	rotMat(15) = 1.0f;

	////////////////////////////////////////////////////
	// Transform the frustum corners
	m3dTransformVector4(nearULT, nearUL, rotMat);
	m3dTransformVector4(nearLLT, nearLL, rotMat);
	m3dTransformVector4(nearURT, nearUR, rotMat);
	m3dTransformVector4(nearLRT, nearLR, rotMat);
	m3dTransformVector4(farULT, farUL, rotMat);
	m3dTransformVector4(farLLT, farLL, rotMat);
	m3dTransformVector4(farURT, farUR, rotMat);
	m3dTransformVector4(farLRT, farLR, rotMat);

	////////////////////////////////////////////////////
	// Derive Plane Equations from points... Points given in
	// counter clockwise order to make normals point inside
	// the Frustum
	// Near and Far Planes
	m3dGetPlaneEquation(nearPlane, nearULT, nearLLT, nearLRT);
	m3dGetPlaneEquation(farPlane, farULT, farURT, farLRT);

	// Top and Bottom Planes
	m3dGetPlaneEquation(topPlane, nearULT, nearURT, farURT);
	m3dGetPlaneEquation(bottomPlane, nearLLT, farLLT, farLRT);

	// Left and right planes
	m3dGetPlaneEquation(leftPlane, nearLLT, nearULT, farULT);
	m3dGetPlaneEquation(rightPlane, nearLRT, farLRT, farURT);
  }



  // Allow expanded version of sphere test
  def TestSphere(x : Float, y : Float, z : Float, fRadius : Float) : Boolean = {
	val vPoint = new M3DVector3f
	vPoint(0) = x;
	vPoint(1) = y;
	vPoint(2) = z;

	return TestSphere(vPoint, fRadius);
  }

  // Test a point against all frustum planes. A negative distance for any
  // single plane means it is outside the frustum. The radius value allows
  // to test for a point (radius = 0), or a sphere. Possibly there might
  // be some gain in an alternative function that saves the addition of
  // zero in this case.
  // Returns false if it is not in the frustum, true if it intersects
  // the Frustum.
  def TestSphere(vPoint : M3DVector3f, fRadius : Float) : Boolean = {
	// Near Plane - See if it is behind me
	var fDist = m3dGetDistanceToPlane(vPoint, nearPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	// Distance to far plane
	fDist = m3dGetDistanceToPlane(vPoint, farPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	fDist = m3dGetDistanceToPlane(vPoint, leftPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	fDist = m3dGetDistanceToPlane(vPoint, rightPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	fDist = m3dGetDistanceToPlane(vPoint, bottomPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	fDist = m3dGetDistanceToPlane(vPoint, topPlane);
	if(fDist + fRadius <= 0.0)
	  return false;

	return true;
  }
}
