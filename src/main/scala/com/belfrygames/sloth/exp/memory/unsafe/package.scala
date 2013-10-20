package com.belfrygames.sloth.exp.memory

import sun.misc.Unsafe

package object unsafe {
  val GetUnsafe = {
    val f = classOf[Unsafe].getDeclaredField("theUnsafe")
    f.setAccessible(true)
    f.get(null).asInstanceOf[Unsafe]
  }
}
