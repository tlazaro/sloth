package com.belfrygames.sloth.glut

object Internal {

  /*
   * An enumeration containing the state of the GLUT execution:
   * initializing, running, or stopping
   */
  object fgExecutionState extends Enumeration {
    val GLUT_EXEC_STATE_INIT, GLUT_EXEC_STATE_RUNNING, GLUT_EXEC_STATE_STOP = Value
  }

  /* A helper structure holding two ints and a boolean */
  class SFG_XYUse(var X: Int, var Y: Int, var Use: Boolean)

  import fgExecutionState._
  import GLUT_EXT._

  class SFG_State {

    /* The default windows' position */
    var Position = new SFG_XYUse(-1, -1, false)
    /* The default windows' size */
    var Size = new SFG_XYUse(300, 300, true)
    var DisplayMode: Int = GLUT_RGBA | GLUT_SINGLE | GLUT_DEPTH /* Display mode for new windows */

    var Initialised: Boolean = _ /* freeglut has been initialised */

    var DirectContext: Int = GLUT_TRY_DIRECT_CONTEXT /* Direct rendering state */

    var ForceIconic: Boolean = _
    /* New top windows are iconified */
    var UseCurrentContext: Boolean = _ /* New windows share with current */

    var GLDebugSwitch: Boolean = _
    /* OpenGL state debugging switch */
    var XSyncSwitch: Boolean = _ /* X11 sync protocol switch */

    var KeyRepeat: Int = GLUT_KEY_REPEAT_ON
    /* Global key repeat mode. */
    var Modifiers: Int = INVALID_MODIFIERS /* Current ALT/SHIFT/CTRL state */

    var FPSInterval: Int = _
    /* Interval between FPS printfs */
    var SwapCount: Int = _
    /* Count of glutSwapBuffer calls */
    var SwapTime: Int = _ /* Time of last SwapBuffers */

    var Time: Long = _
    /* Time that glutInit was called */
    var Timers: List[Any] = List(null, null)
    /* The freeglut timer hooks */
    var FreeTimers: List[Any] = List(null, null) /* The unused timer hooks */

    var IdleCallback: () => Unit = _ /* The global idle callback */

    var ActiveMenus: Int = _
    /* Num. of currently active menus */
    var MenuStateCallback: (Int) => Unit = _
    /* Menu callbacks are global */
    var MenuStatusCallback: (Int, Int, Int) => Unit = _

    var GameModeSize = new SFG_XYUse(640, 480, true) /* Game mode screen's dimensions */

    var GameModeDepth: Int = 16
    /* The pixel depth for game mode */
    var GameModeRefresh: Int = 72 /* The refresh rate for game mode */

    var ActionOnWindowClose: Int = GLUT_ACTION_EXIT /* Action when user closes window */

    var ExecState: fgExecutionState.Value = GLUT_EXEC_STATE_INIT
    /* Used for GLUT termination */
    var ProgramName: String = ""
    /* Name of the invoking program */
    var JoysticksInitialised: Boolean = _
    /* Only initialize if application calls for them */
    var InputDevsInitialised: Boolean = _ /* Only initialize if application calls for them */

    var AuxiliaryBufferNumber: Int = 1
    /* Number of auxiliary buffers */
    var SampleNumber: Int = 4 /* Number of samples per pixel */

    var MajorVersion: Int = 1
    /* Major OpenGL context version */
    var MinorVersion: Int = 0
    /* Minor OpenGL context version */
    var ContextFlags: Int = 0
    /* OpenGL context flags */
    var ContextProfile: Int = 0 /* OpenGL context profile */
  }

  /* The structure used by display initialization in freeglut_init.c */
  class SFG_Display {
    //  #if TARGET_HOST_POSIX_X11
    //  Display* Display; /* The display we are being run in. */
    //  int Screen; /* The screen we are about to use. */
    //  Window RootWindow; /* The screen's root window. */
    //  int Connection; /* The display's connection number */
    //  Atom DeleteWindow; /* The window deletion atom */
    //  Atom State; /* The state atom */
    //  Atom StateFullScreen; /* The full screen atom */
    //
    //  #ifdef X_XF86VidModeGetModeLine
    //  /*
    //   * XF86VidMode may be compilable even if it fails at runtime. Therefore,
    //   * the validity of the VidMode has to be tracked
    //   */
    //  int DisplayModeValid; /* Flag that indicates runtime status*/
    //  XF86VidModeModeLine DisplayMode; /* Current screen's display settings */
    //  int DisplayModeClock; /* The display mode's refresh rate */
    //  int DisplayViewPortX; /* saved X location of the viewport */
    //  int DisplayViewPortY; /* saved Y location of the viewport */
    //  int DisplayPointerX; /* saved X location of the pointer */
    //  int DisplayPointerY; /* saved Y location of the pointer */
    //
    //  #endif /* X_XF86VidModeGetModeLine */
    //
    //  #elif TARGET_HOST_MS_WINDOWS
    //  HINSTANCE Instance; /* The application's instance */
    //  DEVMODE DisplayMode; /* Desktop's display settings */
    //
    //  #endif

    var ScreenWidth: Int = _;
    /* The screen's width in pixels */
    var ScreenHeight: Int = _;
    /* The screen's height in pixels */
    var ScreenWidthMM: Int = _;
    /* The screen's width in milimeters */
    var ScreenHeightMM: Int = _; /* The screen's height in milimeters */
  };
}