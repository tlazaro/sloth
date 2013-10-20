package com.belfrygames.sloth

import org.specs2.mutable._

class SafeVsUnsafe extends Specification {

  "The 'Hello world' string" should {
    "contain 11 characters" in {
      measure {
        "Hello world" must have size (11)
      }
    }

    "start with 'Hello'" in {
      measure {
        "Hello world" must startWith("Hello")
      }
    }

    "end with 'world'" in {
      measure {
        "Hello world" must endWith("world")
      }
    }
  }

  def measure[A](block: => A): A = {
    val start = System.nanoTime
    val res = block
    val elapsed = System.nanoTime - start

    textFragment(s"${is.starts} took: ${elapsed / 1000000f}ms")
    res
  }
}