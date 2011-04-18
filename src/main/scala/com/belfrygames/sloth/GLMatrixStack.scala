package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D._

object GLT_STACK_ERROR extends Enumeration {
  val GLT_STACK_NOERROR , GLT_STACK_OVERFLOW, GLT_STACK_UNDERFLOW = Value
}

class GLMatrixStack(private val stackDepth : Int = 64) {
  import GLT_STACK_ERROR._
  
  var lastError : GLT_STACK_ERROR.Value = GLT_STACK_NOERROR;
  var stackPointer : Int = 0;
  var pStack = new M3DMatrix44fArray(stackDepth)

  m3dLoadIdentity44(pStack(0))

  // Temporary matrixes
  private[this] val mTemp = new M3DMatrix44f
  private[this] val mTemp2 = new M3DMatrix44f

  @inline def LoadIdentity() {
	m3dLoadIdentity44(pStack(stackPointer));
  }

  @inline def LoadMatrix(mMatrix : M3DMatrix44f) {
	m3dCopyMatrix44(pStack(stackPointer), mMatrix);
  }

  @inline def LoadMatrix(frame : GLFrame) {
	frame.GetMatrix(mTemp);
	LoadMatrix(mTemp);
  }
  
  @inline def MultMatrix(mMatrix : M3DMatrix44f) {
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mMatrix);
  }

  @inline def MultMatrix(frame : GLFrame) {
	frame.GetMatrix(mTemp2);
	MultMatrix(mTemp2);
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
	m3dScaleMatrix44(mTemp2, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }

  def Translate(x : Float, y : Float, z : Float) {
	m3dTranslationMatrix44(mTemp2, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }

  def Rotate(angle : Float, x : Float, y : Float, z : Float) {
	m3dRotationMatrix44(mTemp2, m3dDegToRad(angle).toFloat, x, y, z);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }


  // I've always wanted vector versions of these
  def Scalev(vScale : M3DVector3f) {
	m3dScaleMatrix44(mTemp2, vScale);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }


  def Translatev(vTranslate : M3DVector3f) {
	m3dLoadIdentity44(mTemp2);
	m3dSetMatrixColumn44(mTemp2, vTranslate, 3);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }

  def Translatev(vTranslate : M3DVector4f) {
	m3dLoadIdentity44(mTemp2);
	m3dSetMatrixColumn44(mTemp2, vTranslate, 3);
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
  }


  def Rotatev(angle : Float, vAxis : M3DVector3f) {
	m3dRotationMatrix44(mTemp2, m3dDegToRad(angle).toFloat, vAxis(0), vAxis(1), vAxis(2));
	m3dCopyMatrix44(mTemp, pStack(stackPointer));
	m3dMatrixMultiply44(pStack(stackPointer), mTemp, mTemp2);
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
	frame.GetMatrix(mTemp);
	PushMatrix(mTemp);
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
