package com.belfrygames.sloth.glut

object GLUT_EXT {
  /*
   * Additional GLUT Key definitions for the Special key function
   */
  val GLUT_KEY_NUM_LOCK = 0x006D
  val GLUT_KEY_BEGIN = 0x006E
  val GLUT_KEY_DELETE = 0x006F

  /*
   * GLUT API Extension macro definitions -- behaviour when the user clicks on an "x" to close a window
   */
  val GLUT_ACTION_EXIT = 0
  val GLUT_ACTION_GLUTMAINLOOP_RETURNS = 1
  val GLUT_ACTION_CONTINUE_EXECUTION = 2

  /*
   * Create a new rendering context when the user opens a new window?
   */
  val GLUT_CREATE_NEW_CONTEXT = 0
  val GLUT_USE_CURRENT_CONTEXT = 1

  /*
   * Direct/Indirect rendering context options (has meaning only in Unix/X11)
   */
  val GLUT_FORCE_INDIRECT_CONTEXT = 0
  val GLUT_ALLOW_DIRECT_CONTEXT = 1
  val GLUT_TRY_DIRECT_CONTEXT = 2
  val GLUT_FORCE_DIRECT_CONTEXT = 3

  /*
   * GLUT API Extension macro definitions -- the glutGet parameters
   */
  val GLUT_INIT_STATE = 0x007C

  val GLUT_ACTION_ON_WINDOW_CLOSE = 0x01F9

  val GLUT_WINDOW_BORDER_WIDTH = 0x01FA
  val GLUT_WINDOW_HEADER_HEIGHT = 0x01FB

  val GLUT_VERSION = 0x01FC

  val GLUT_RENDERING_CONTEXT = 0x01FD
  val GLUT_DIRECT_RENDERING = 0x01FE

  val GLUT_FULL_SCREEN = 0x01FF

  /*
   * New tokens for glutInitDisplayMode.
   * Only one GLUT_AUXn bit may be used at a time.
   * Value 0x0400 is defined in OpenGLUT.
   */
  val GLUT_AUX = 0x1000

  val GLUT_AUX1 = 0x1000
  val GLUT_AUX2 = 0x2000
  val GLUT_AUX3 = 0x4000
  val GLUT_AUX4 = 0x8000

  /*
   * Context-related flags, see freeglut_state.c
   */
  val GLUT_INIT_MAJOR_VERSION = 0x0200
  val GLUT_INIT_MINOR_VERSION = 0x0201
  val GLUT_INIT_FLAGS = 0x0202
  val GLUT_INIT_PROFILE = 0x0203

  /*
   * Flags for glutInitContextFlags, see freeglut_init.c
   */
  val GLUT_DEBUG = 0x0001
  val GLUT_FORWARD_COMPATIBLE = 0x0002


  /*
   * Flags for glutInitContextProfile, see freeglut_init.c
   */
  val GLUT_CORE_PROFILE = 0x0001
  val GLUT_COMPATIBILITY_PROFILE = 0x0002
}
