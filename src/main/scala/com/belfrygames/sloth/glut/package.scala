package com.belfrygames.sloth

package object glut {
  val INVALID_MODIFIERS = 0xffffffff

  /*
   * GLUT API macro definitions -- the display mode definitions
   */
  val  GLUT_RGB = 0x0000
  val  GLUT_RGBA = 0x0000
  val  GLUT_INDEX = 0x0001
  val  GLUT_SINGLE = 0x0000
  val  GLUT_DOUBLE = 0x0002
  val  GLUT_ACCUM = 0x0004
  val  GLUT_ALPHA = 0x0008
  val  GLUT_DEPTH = 0x0010
  val  GLUT_STENCIL = 0x0020
  val  GLUT_MULTISAMPLE = 0x0080
  val  GLUT_STEREO = 0x0100
  val  GLUT_LUMINANCE = 0x0200

  /*
   * GLUT API macro definitions -- additional keyboard and joystick definitions
   */
  val  GLUT_KEY_REPEAT_OFF =                0x0000
  val  GLUT_KEY_REPEAT_ON  =	            0x0001
  val  GLUT_KEY_REPEAT_DEFAULT =            0x0002
}