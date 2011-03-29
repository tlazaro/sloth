package com.belfrygames

package object sloth {
  class VarArgs[T] (args : Seq[T], var position : Int = 0) {
	def arg[A] : A  = {
	  val ret = args(position).asInstanceOf[A]
	  position += 1
	  ret
	}
  }
}
