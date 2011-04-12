package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D._

object GLT_STACK_ERROR extends Enumeration {
  val GLT_STACK_NOERROR , GLT_STACK_OVERFLOW, GLT_STACK_UNDERFLOW = Value
}

class GLMatrixStack(private val stackDepth : Int = 64) {
  import GLT_STACK_ERROR._
  
  var lastError : GLT_STACK_ERROR.Value = GLT_STACK_NOERROR;
  var stackPointer : Int = 0;
  var pStack = M3DVector.array[M3DMatrix44f](stackDepth)

  m3dLoadIdentity44(pStack(0))


  @inline def LoadIdentity() {
	m3dLoadIdentity44(pStack(stackPointer));
  }

  @inline def LoadMatrix(mMatrix : M3DMatrix44f) {
	m3dCopyMatrix44(pStack(stackPointer), mMatrix);
  }

  @inline def LoadMatrix(frame : GLFrame) {
	val m = new M3DMatrix44f;
	frame.GetMatrix(m);
	LoadMatrix(m);
  }

  @inline def MultMatrix(mMatrix : M3DMatrix44f) {
	val mTemp = new M3DMatrix44f
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mMatrix);
  }

  @inline def MultMatrix(frame : GLFrame) {
	val m = new M3DMatrix44f
	frame.GetMatrix(m);
	MultMatrix(m);
  }

  @inline def PushMatrix() {
	if(stackPointer < stackDepth) {
	  stackPointer += 1;
	  m3dCopyMatrix44(pStack(stackPointer), pStack(stackPointer-1));
	} else {
	  lastError = GLT_STACK_OVERFLOW;
	}
  }

  @inline def PopMatrix() {
	if(stackPointer > 0)
	  stackPointer -= 1;
	else
	  lastError = GLT_STACK_UNDERFLOW;
  }

  def Scale(x : Float, y : Float, z : Float) {
	val mTemp = new M3DMatrix44f
	val mScale = new M3DMatrix44f
	
	m3dScaleMatrix44(mScale, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mScale);
  }


  def Translate(x : Float, y : Float, z : Float) {
	val mTemp = new M3DMatrix44f
	val mScale = new M3DMatrix44f
	
	m3dTranslationMatrix44(mScale, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mScale);
  }

  def Rotate(angle : Float, x : Float, y : Float, z : Float) {
	val mTemp = new M3DMatrix44f
	val mRotate = new M3DMatrix44f

	m3dRotationMatrix44(mRotate, m3dDegToRad(angle).toFloat, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mRotate);
  }


  // I've always wanted vector versions of these
  def Scalev(vScale : M3DVector3f) {
	val mTemp = new M3DMatrix44f
	val mScale = new M3DMatrix44f
	
	m3dScaleMatrix44(mScale, vScale);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mScale);
  }


  def Translatev(vTranslate : M3DVector3f) {
	val mTemp = new M3DMatrix44f
	val mTranslate = new M3DMatrix44f

	m3dLoadIdentity44(mTranslate);
	m3dSetMatrixColumn44(mTranslate, vTranslate, 3);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTranslate);
  }


  def Rotatev(angle : Float, vAxis : M3DVector3f) {
	val mTemp = new M3DMatrix44f
	val mRotation = new M3DMatrix44f

	m3dRotationMatrix44(mRotation, m3dDegToRad(angle).toFloat, vAxis(0), vAxis(1), vAxis(2));
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mRotation);
  }


  // I've also always wanted to be able to do this
  def PushMatrix(mMatrix : M3DMatrix44f) {
	if(stackPointer < stackDepth) {
	  stackPointer += 1;
	  m3dCopyMatrix44(pStack(stackPointer), mMatrix);
	}
	else
	  lastError = GLT_STACK_OVERFLOW;
  }

  def PushMatrix(frame : GLFrame) {
	val m = new M3DMatrix44f
	frame.GetMatrix(m);
	PushMatrix(m);
  }

  // Two different ways to get the matrix
  def GetMatrix() : M3DMatrix44f = { return pStack(stackPointer); }
  def GetMatrix(mMatrix : M3DMatrix44f) { m3dCopyMatrix44(mMatrix, pStack(stackPointer)); }


  @inline def GetLastError() : GLT_STACK_ERROR.Value = {
	val retval = lastError;
	lastError = GLT_STACK_NOERROR;
	return retval;
  }
}
