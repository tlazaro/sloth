package com.belfrygames.sloth

import java.awt.event._
import java.awt._
import javax.imageio._
import javax.swing._

import java.nio._

import org.lwjgl.opengl.{Display => GLDisplay}
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL15._
import org.lwjgl.util.glu.GLU._

object App {
  val f = new JFrame()
  var keepAlive = true
  var lastRender = 0L
  var elapsed = 0.0
  var frames = 0L

  lazy val c = new Canvas()

  def setupWindow() {
    f.setSize(1024, 768)
	f.setTitle("Test Scala")
	f.setResizable(false)
	f.setLayout(new BorderLayout())
	f.addWindowListener(new WindowAdapter(){
		override def windowClosing(e: WindowEvent): Unit = {
		  keepAlive = false
		  f.dispose()
		}

		override def windowLostFocus(e : WindowEvent) {
		  println("regained focus")
		  f.requestFocus
		}
	  })

	c.setSize(1024, 768)
	f.add(BorderLayout.CENTER, c)

	f.setVisible(true)
    f.addKeyListener(new KeyListener() {
		def keyPressed(e : KeyEvent) {
		  import KeyEvent._
		  e.getKeyCode match {
			case VK_W => println("Pressed W")
			case VK_A => println("Pressed A")
			case VK_S => println("Pressed S")
			case VK_D => println("Pressed D")
			case _ =>
		  }
		}
		def keyReleased(e : KeyEvent) {
		  import KeyEvent._
		  e.getKeyCode match {
			case VK_W => println("Released W")
			case VK_A => println("Released A")
			case VK_S => println("Released S")
			case VK_D => println("Released D")
			case _ =>
		  }
		}
		def keyTyped(e : KeyEvent) {

		}
	  })
  }

  def setupOpenGL() {
    GLDisplay.setFullscreen(false)
	GLDisplay.setVSyncEnabled(false)
	GLDisplay.setParent(c)
	GLDisplay.create()

	// Setup GL
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
	glViewport(0, 0, 1024, 768)
  }

  def setupLighting() {
    //val lightAmbient = toFloatBuffer(1.0f, 1.0f, 1.0f, 1.0f)
	//val lightDiffuse = toFloatBuffer(0.2f, 0.3f, 0.6f, 1.0f)
	//val matAmbient = toFloatBuffer(0.6f, 0.6f, 0.6f, 1.0f)
	//val matDiffuse = toFloatBuffer(0.6f, 0.6f, 0.6f, 1.0f)

	//glEnable(GL_LIGHTING)
	//glEnable(GL_LIGHT0)

	//glMaterial(GL_FRONT_AND_BACK, GL_AMBIENT, matAmbient)
	//glMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE, matDiffuse)

	//glLight(GL_LIGHT0, GL_AMBIENT, lightAmbient)
	//glLight(GL_LIGHT0, GL_DIFFUSE, lightDiffuse)

	/*glEnable(GL_DEPTH_TEST)
	 glDepthFunc(GL_LEQUAL)

	 glEnable(GL_CULL_FACE)
	 glShadeModel(GL_SMOOTH)

	 glEnable(GL_RESCALE_NORMAL)*/
  }

  def main(args: Array[String]): Unit = {
    setupWindow()

    setupOpenGL()
    setupLighting()

	val sizes = org.lwjgl.BufferUtils.createFloatBuffer(16)
	glGetFloat(GL_POINT_SIZE_RANGE, sizes)
	val step = glGetFloat(GL_POINT_SIZE_GRANULARITY)
	println("Point size range: [" + sizes.get(0) + ", " + sizes.get(1) + "]")
	println("Step: " + step)

    mainLoop()
	cleanUp()
  }

  def cleanUp() {
    GLDisplay.destroy()
	f.dispose()
	System.exit(0)
  }

  val verts = Array(Array(-0.5f, 0.0f, 0.0f), Array(0.5f, 0.0f, 0.0f), Array(0.0f, 0.5f, 0.0f)).reverse
  def drawTriangle() {
    glColor4f(1.0f, 0.0f, 0.0f, 1.0f )
    glBegin(GL_TRIANGLES)
    for (vert <- verts) {
      glVertex3f(vert(0), vert(1), vert(2))
    }
    glEnd()
  }


  def mainLoop() {
    // Rendering
	while (keepAlive) {
	  GLDisplay.update()

	  if (GLDisplay.isCloseRequested()) {
		keepAlive = false
	  }

	  val currentRender = System.nanoTime()
	  val time = (currentRender - lastRender) / 1000000000.0

	  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT)

	  /*glLoadIdentity()
	   glColor4f(1.0f, 1.0f, 1.0f, 1.0f)

	   elapsed += time
	   frames += 1
	   if (elapsed > 1.0) {
	   elapsed = 0.0
	   info("FPS: " + frames)
	   frames = 0
	   }*/

	  render()

	  lastRender = currentRender
	}
  }

  def render() = {
    drawTriangle()
  }

  // Utility
  def toFloatBuffer(args: Float*) = {
    val buffer = org.lwjgl.BufferUtils.createFloatBuffer(args.length)

    for (f <- args) {
      buffer.put(f)
    }
    buffer.flip()

    buffer
  }
}
