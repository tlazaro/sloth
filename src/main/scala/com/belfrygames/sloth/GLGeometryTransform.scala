package com.belfrygames.sloth

import com.belfrygames.sloth.Math3D._

class GLGeometryTransform {
  val _mModelViewProjection = new M3DMatrix44f
  val _mNormalMatrix = new M3DMatrix33f

  var _mModelView : GLMatrixStack = _
  var _mProjection : GLMatrixStack = _

  @inline def SetModelViewMatrixStack(mModelView : GLMatrixStack) { _mModelView = mModelView; }

  @inline def SetProjectionMatrixStack(mProjection : GLMatrixStack) { _mProjection = mProjection; }

  @inline def SetMatrixStacks(mModelView : GLMatrixStack, mProjection : GLMatrixStack) {
	_mModelView = mModelView;
	_mProjection = mProjection;
  }

  def GetModelViewProjectionMatrix() : M3DMatrix44f = {
	m3dMatrixMultiply44(_mModelViewProjection, _mProjection.GetMatrix(), _mModelView.GetMatrix());
	return _mModelViewProjection;
  }

  @inline def GetModelViewMatrix() : M3DMatrix44f = _mModelView.GetMatrix()
  @inline def GetProjectionMatrix() : M3DMatrix44f = _mProjection.GetMatrix()

  def GetNormalMatrix(bNormalize : Boolean = false) : M3DMatrix33f = {
	m3dExtractRotationMatrix33(_mNormalMatrix, GetModelViewMatrix());

	if(bNormalize) {
	  m3dNormalizeVector3(_mNormalMatrix, 0);
	  m3dNormalizeVector3(_mNormalMatrix, 3);
	  m3dNormalizeVector3(_mNormalMatrix, 6);
	}

	return _mNormalMatrix;
  }
}
