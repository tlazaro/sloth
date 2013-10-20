package com.belfrygames.sloth.exp.memory.unsafe

import UnsafeMath3D._

class UnsafeGLGeometryTransform {
  val _mModelViewProjection = M3DMatrix44f()
  val _mNormalMatrix = M3DMatrix33f()

  var _mModelView: UnsafeGLMatrixStack = _
  var _mProjection: UnsafeGLMatrixStack = _

  @inline def SetModelViewMatrixStack(mModelView: UnsafeGLMatrixStack) {
    _mModelView = mModelView;
  }

  @inline def SetProjectionMatrixStack(mProjection: UnsafeGLMatrixStack) {
    _mProjection = mProjection;
  }

  @inline def SetMatrixStacks(mModelView: UnsafeGLMatrixStack, mProjection: UnsafeGLMatrixStack) {
    _mModelView = mModelView;
    _mProjection = mProjection;
  }

  def GetModelViewProjectionMatrix(): M3DMatrix44f = {
    m3dMatrixMultiply44(_mModelViewProjection, _mProjection.GetMatrix(), _mModelView.GetMatrix());
    return _mModelViewProjection;
  }

  @inline def GetModelViewMatrix(): M3DMatrix44f = _mModelView.GetMatrix()

  @inline def GetProjectionMatrix(): M3DMatrix44f = _mProjection.GetMatrix()

  def GetNormalMatrix(bNormalize: Boolean = false): M3DMatrix33f = {
    m3dExtractRotationMatrix33(_mNormalMatrix, GetModelViewMatrix());

    if (bNormalize) {
      m3dNormalizeVector3(_mNormalMatrix, 0);
      m3dNormalizeVector3(_mNormalMatrix, 3);
      m3dNormalizeVector3(_mNormalMatrix, 6);
    }

    return _mNormalMatrix;
  }
}
