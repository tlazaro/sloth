package org.lwjgl.opengl

import org.lwjgl.BufferChecks

object UnsafeGL20 {
  def glUniformMatrix4(location: Int, transpose: Boolean, address: Long) {
    val caps = GLContext.getCapabilities()
    val function_pointer = caps.glUniformMatrix4fv
    BufferChecks.checkFunctionAddress(function_pointer)
    GL20.nglUniformMatrix4fv(location, 1, transpose, address, function_pointer)
  }

  def glUniform4(location: Int, address: Long) {
    val caps = GLContext.getCapabilities()
    val function_pointer = caps.glUniform4iv
    BufferChecks.checkFunctionAddress(function_pointer)
    GL20.nglUniform4iv(location, 1, address, function_pointer)
  }

  def glUniform3(location: Int, address: Long) {
    val caps = GLContext.getCapabilities()
    val function_pointer = caps.glUniform3iv
    BufferChecks.checkFunctionAddress(function_pointer)
    GL20.nglUniform3iv(location, 1, address, function_pointer)
  }
}
