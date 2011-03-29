package com.belfrygames.sloth.glut

import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JFrame

import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.{Display => GLDisplay}

import GLUT_EXT._
import Internal._
import Internal.fgExecutionState._

object GLUT {

  val fgState = new SFG_State()

  def fgError(fmt : String) {
	println("Sloth GLUT")
	println(fgState.ProgramName)
	String.format(fmt)
  }

  def fgError(fmt : String, message : String*) {
	println("Sloth GLUT")
	println(fgState.ProgramName)
	String.format(fmt, message)
  }

  def fgSystemTime() = System.nanoTime

  def fgCreateStructure() {
	/*
     * We will be needing two lists: the first containing windows,
     * and the second containing the user-defined menus.
     * Also, no current window/menu is set, as none has been created yet.
     */

//    fgListInit(&fgStructure.Windows);
//    fgListInit(&fgStructure.Menus);
//    fgListInit(&fgStructure.WindowsToDestroy);
//
//    fgStructure.CurrentWindow = NULL;
//    fgStructure.CurrentMenu = NULL;
//    fgStructure.MenuContext = NULL;
//    fgStructure.GameModeWindow = NULL;
//    fgStructure.WindowID = 0;
//    fgStructure.MenuID = 0;
  }
  
  def glutInit(args : Array[String]) {
	val displayName = ""
	val geometry = ""

	if( fgState.Initialised )
	  fgError( "illegal glutInit() reinitialization attempt" )

	if (args.size > 0) {
	  fgState.ProgramName = args(0)
	}

	fgCreateStructure();

	/* Get start time */
	fgState.Time = fgSystemTime();
  }
//	  {
//		  char* displayName = NULL;
//		  char* geometry = NULL;
//		  int i, j, argc = *pargc;


//
//		  /* check if GLUT_FPS env var is set */
//	  #ifndef _WIN32_WCE
//		  {
//		  /* will return true for VC8 (VC2005) and higher */
//	  #if TARGET_HOST_MS_WINDOWS && ( _MSC_VER >= 1400 ) && HAVE_ERRNO
//			  char* fps = NULL;
//			  err = _dupenv_s( &fps, &sLen, "GLUT_FPS" );
//			  if (err)
//				  fgError("Error getting GLUT_FPS environment variable");
//	  #else
//			  const char *fps = getenv( "GLUT_FPS" );
//	  #endif
//			  if( fps )
//			  {
//				  int interval;
//				  sscanf( fps, "%d", &interval );
//
//				  if( interval <= 0 )
//					  fgState.FPSInterval = 5000;  /* 5000 millisecond default */
//				  else
//					  fgState.FPSInterval = interval;
//			  }
//		  /* will return true for VC8 (VC2005) and higher */
//	  #if TARGET_HOST_MS_WINDOWS && ( _MSC_VER >= 1400 ) && HAVE_ERRNO
//			  free ( fps );  fps = NULL;  /* dupenv_s allocates a string that we must free */
//	  #endif
//		  }
//
//		  /* will return true for VC8 (VC2005) and higher */
//	  #if TARGET_HOST_MS_WINDOWS && ( _MSC_VER >= 1400 ) && HAVE_ERRNO
//		  err = _dupenv_s( &displayName, &sLen, "DISPLAY" );
//		  if (err)
//			  fgError("Error getting DISPLAY environment variable");
//	  #else
//		  displayName = getenv( "DISPLAY" );
//	  #endif
//
//		  for( i = 1; i < argc; i++ )
//		  {
//			  if( strcmp( argv[ i ], "-display" ) == 0 )
//			  {
//				  if( ++i >= argc )
//					  fgError( "-display parameter must be followed by display name" );
//
//				  displayName = argv[ i ];
//
//				  argv[ i - 1 ] = NULL;
//				  argv[ i     ] = NULL;
//				  ( *pargc ) -= 2;
//			  }
//			  else if( strcmp( argv[ i ], "-geometry" ) == 0 )
//			  {
//				  if( ++i >= argc )
//					  fgError( "-geometry parameter must be followed by window "
//							   "geometry settings" );
//
//				  geometry = argv[ i ];
//
//				  argv[ i - 1 ] = NULL;
//				  argv[ i     ] = NULL;
//				  ( *pargc ) -= 2;
//			  }
//			  else if( strcmp( argv[ i ], "-direct" ) == 0)
//			  {
//				  if( fgState.DirectContext == GLUT_FORCE_INDIRECT_CONTEXT )
//					  fgError( "parameters ambiguity, -direct and -indirect "
//						  "cannot be both specified" );
//
//				  fgState.DirectContext = GLUT_FORCE_DIRECT_CONTEXT;
//				  argv[ i ] = NULL;
//				  ( *pargc )--;
//			  }
//			  else if( strcmp( argv[ i ], "-indirect" ) == 0 )
//			  {
//				  if( fgState.DirectContext == GLUT_FORCE_DIRECT_CONTEXT )
//					  fgError( "parameters ambiguity, -direct and -indirect "
//						  "cannot be both specified" );
//
//				  fgState.DirectContext = GLUT_FORCE_INDIRECT_CONTEXT;
//				  argv[ i ] = NULL;
//				  (*pargc)--;
//			  }
//			  else if( strcmp( argv[ i ], "-iconic" ) == 0 )
//			  {
//				  fgState.ForceIconic = GL_TRUE;
//				  argv[ i ] = NULL;
//				  ( *pargc )--;
//			  }
//			  else if( strcmp( argv[ i ], "-gldebug" ) == 0 )
//			  {
//				  fgState.GLDebugSwitch = GL_TRUE;
//				  argv[ i ] = NULL;
//				  ( *pargc )--;
//			  }
//			  else if( strcmp( argv[ i ], "-sync" ) == 0 )
//			  {
//				  fgState.XSyncSwitch = GL_TRUE;
//				  argv[ i ] = NULL;
//				  ( *pargc )--;
//			  }
//		  }
//
//		  /* Compact {argv}. */
//		  for( i = j = 1; i < *pargc; i++, j++ )
//		  {
//			  /* Guaranteed to end because there are "*pargc" arguments left */
//			  while ( argv[ j ] == NULL )
//				  j++;
//			  if ( i != j )
//				  argv[ i ] = argv[ j ];
//		  }
//
//	  #endif /* _WIN32_WCE */
//
//		  /*
//		   * Have the display created now. If there wasn't a "-display"
//		   * in the program arguments, we will use the DISPLAY environment
//		   * variable for opening the X display (see code above):
//		   */
//		  fghInitialize( displayName );
//		  /* will return true for VC8 (VC2005) and higher */
//	  #if TARGET_HOST_MS_WINDOWS && ( _MSC_VER >= 1400 ) && HAVE_ERRNO
//		  free ( displayName );  displayName = NULL;  /* dupenv_s allocates a string that we must free */
//	  #endif
//
//		  /*
//		   * Geometry parsing deffered until here because we may need the screen
//		   * size.
//		   */
//
//		  if (geometry )
//		  {
//			  unsigned int parsedWidth, parsedHeight;
//			  int mask = XParseGeometry( geometry,
//										 &fgState.Position.X, &fgState.Position.Y,
//										 &parsedWidth, &parsedHeight );
//			  /* TODO: Check for overflow? */
//			  fgState.Size.X = parsedWidth;
//			  fgState.Size.Y = parsedHeight;
//
//			  if( (mask & (WidthValue|HeightValue)) == (WidthValue|HeightValue) )
//				  fgState.Size.Use = GL_TRUE;
//
//			  if( mask & XNegative )
//				  fgState.Position.X += fgDisplay.ScreenWidth - fgState.Size.X;
//
//			  if( mask & YNegative )
//				  fgState.Position.Y += fgDisplay.ScreenHeight - fgState.Size.Y;
//
//			  if( (mask & (XValue|YValue)) == (XValue|YValue) )
//				  fgState.Position.Use = GL_TRUE;
//		  }
//	  }

  def glutInitDisplayMode(displayMode : Int) = fgState.DisplayMode = displayMode

  def glutInitWindowSize(width : Int, height : Int) {
	fgState.Size.X = width;
    fgState.Size.Y = height;

	fgState.Size.Use = ( width > 0 ) && ( height > 0 )
  }

  /*
   * A call to this function makes us sure that the Display and Structure
   * subsystems have been properly initialized and are ready to be used
   */
  def  FREEGLUT_EXIT_IF_NOT_INITIALISED( string : String) {
	if ( ! fgState.Initialised ) {
	  fgError ( " ERROR:  Function <%s> called without first calling 'glutInit'.", string ) ;
	}
  }

  def  FREEGLUT_INTERNAL_ERROR_EXIT_IF_NOT_INITIALISED( string : String)  {
	if ( ! fgState.Initialised ) {
	  fgError ( " ERROR:  Internal <%s> function called without first calling 'glutInit'.", (string) ) ;
	}
  }

  def  FREEGLUT_INTERNAL_ERROR_EXIT( cond : Boolean, string : String, function : String)  {
	if ( ! ( cond ) )	{
	  fgError ( " ERROR:  Internal error <%s> in function %s", string, function ) ;
	}
  }

  def glutCreateWindow(title : String) {
	/* XXX GLUT does not exit; it simply calls "glutInit" quietly if the
     * XXX application has not already done so.  The "freeglut" community
     * XXX decided not to go this route (freeglut-developer e-mail from
     * XXX Steve Baker, 12/16/04, 4:22 PM CST, "Re: [Freeglut-developer]
     * XXX Desired 'freeglut' behaviour when there is no current window"
     */
    FREEGLUT_EXIT_IF_NOT_INITIALISED ( "glutCreateWindow" );

    return fgCreateWindow(null, title, fgState.Position.Use, fgState.Position.X, fgState.Position.Y, fgState.Size.Use, fgState.Size.X, fgState.Size.Y, false, false).ID;
  }

  class SFG_Window (val ID : Int) {
	val f = new JFrame()
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	lazy val c = new Canvas()
  }
  
  object WindowHandler extends ComponentListener {
	var mainWindow : Option[SFG_Window] = None
	var reshapeFunc : Option[(Int, Int) => Unit] = None
	var displayFunc : Option[() => Unit] = None
	var specialFunc : Option[(Int, Int, Int) => Unit] = None

	def componentHidden(e : ComponentEvent) {
	  println(e.getComponent().getClass().getName() + " --- Hidden");
	  println(e)
    }

    def componentMoved(e : ComponentEvent) {
	  println(e.getComponent().getClass().getName() + " --- Moved");
	  println(e)
    }

	def componentResized(e : ComponentEvent) {
	  println(e.getComponent().getClass().getName() + " --- Resized");
	  println(e)
	  for (window <- mainWindow; func <- reshapeFunc) {
		func.apply(window.f.getWidth, window.f.getHeight)
	  }
    }

	def componentShown(e : ComponentEvent) {
	  println(e.getComponent().getClass().getName() + " --- Shown");
	  println(e)
    }
  }

  def fgCreateWindow(parent : SFG_Window, title : String, positionUse : Boolean, x : Int, y : Int, sizeUse : Boolean, w : Int, h : Int, gameMode : Boolean, isMenu : Boolean) : SFG_Window = {
	val window = new SFG_Window(0)

	WindowHandler.mainWindow = Some(window)

    window.f.setSize(w, h)
	window.f.setTitle(title)
	window.f.setResizable(false)
	window.f.setLayout(new BorderLayout())

	window.c.setSize(w, h)
	window.f.add(BorderLayout.CENTER, window.c)

	window.f.setVisible(true)

	window.f.addComponentListener(WindowHandler)

	GLDisplay.setFullscreen(false)
	GLDisplay.setVSyncEnabled(false)
	GLDisplay.setParent(window.c)
	GLDisplay.create()

	// Setup GL
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
	glViewport(0, 0, w, h)

	window
  }

  def glutReshapeFunc(func : (Int, Int) => Unit) {
	WindowHandler.reshapeFunc = Some(func)
  }

  def glutDisplayFunc(func : () => Unit) {
	WindowHandler.displayFunc = Some(func)
  }

  def glutSpecialFunc(func : (Int, Int, Int) => Unit) {
	WindowHandler.specialFunc = Some(func)
  }

  var keepAlive = true
  var lastRender = 0L

  private def keyboardToGlut(key : Int) = {
	import org.lwjgl.input.Keyboard._
	import com.belfrygames.sloth.glut.STD._
	key match {
	  case KEY_F1 => GLUT_KEY_F1
	  case KEY_F2 => GLUT_KEY_F2
	  case KEY_F3 => GLUT_KEY_F3
	  case KEY_F4 => GLUT_KEY_F4
	  case KEY_F5 => GLUT_KEY_F5
	  case KEY_F6 => GLUT_KEY_F6
	  case KEY_F7 => GLUT_KEY_F7
	  case KEY_F8 => GLUT_KEY_F8
	  case KEY_F9 => GLUT_KEY_F9
	  case KEY_F10 => GLUT_KEY_F10
	  case KEY_F11 => GLUT_KEY_F11
	  case KEY_F12 => GLUT_KEY_F12
	  case KEY_LEFT => GLUT_KEY_LEFT
	  case KEY_UP => GLUT_KEY_UP
	  case KEY_RIGHT => GLUT_KEY_RIGHT
	  case KEY_DOWN => GLUT_KEY_DOWN
	  case KEY_PRIOR => GLUT_KEY_PAGE_UP // ????
	  case KEY_NEXT => GLUT_KEY_PAGE_DOWN // ????
	  case KEY_HOME => GLUT_KEY_HOME
	  case KEY_END => GLUT_KEY_END
	  case KEY_INSERT => GLUT_KEY_INSERT
	  case _ => 0
	}
  }
  
  def glutMainLoop() {
	// Rendering
	while (keepAlive) {
	  GLDisplay.update()

	  if (GLDisplay.isCloseRequested()) {
		keepAlive = false
	  }

	  val currentRender = System.nanoTime()
	  val time = (currentRender - lastRender) / 1000000000.0

	  for (window <- WindowHandler.mainWindow) {
		for (func <- WindowHandler.displayFunc) func.apply()

		for (func <- WindowHandler.specialFunc) {
		  while (Keyboard.next()) {
			// Is this what it's supposed to send???
			func.apply(keyboardToGlut(Keyboard.getEventKey), Mouse.getX, Mouse.getY)
		  }
		}
	  }

	  lastRender = currentRender
	}
  }

  def glutSwapBuffers() {
	GLDisplay.swapBuffers
  }

  def glutPostRedisplay() {
	GLDisplay.update() // <- is this ok?
  }
}
