package com.belfrygames.sloth.exp

import scala.reflect.ClassTag

object Church {

  import Booleans._

  // References [Jim] = http://jim-mcbeath.blogspot.com/2008/11/practical-church-numerals-in-scala.html
  // References [Michid] = http://michid.wordpress.com/2008/03/12/puzzle-the-depth-of-a-type-solution/

  case class Equals[A >: B <: B, B]()

  class IsTrue[B <: Bool]

  implicit val trueIsTrue = new IsTrue[True]

  // [Jim]

  trait CInt {
    type Pred <: CInt
    type Succ <: CInt

    type Add[N <: CInt] <: CInt
    type Sub[N <: CInt] <: CInt

    type Neg <: CInt
    type IsNeg <: Bool
  }

  trait CPos extends CInt

  trait CNeg extends CInt

  class CSucc[P <: CPos] extends CPos {
    type Pred = P
    type Succ = CSucc[CSucc[P]]

    type Add[N <: CInt] = P#Add[N]#Succ
    type Sub[N <: CInt] = P#Sub[N]#Succ

    type Neg = P#Neg#Pred

    type IsNeg = False
  }

  class CPred[S <: CNeg] extends CNeg {
    type Pred = CPred[CPred[S]]
    type Succ = S

    type Add[N <: CInt] = S#Add[N]#Pred
    type Sub[N <: CInt] = S#Sub[N]#Pred

    type Neg = S#Neg#Succ

    type IsNeg = True
  }

  class _0 extends CPos with CNeg {
    type Pred = CPred[_0]
    type Succ = CSucc[_0]

    type Add[N <: CInt] = N
    type Sub[N <: CInt] = N#Neg

    type Neg = _0

    type IsNeg = False
  }

  // [/Jim]

  type _1 = _0#Succ
  type _2 = _1#Succ
  type _3 = _2#Succ
  type _4 = _3#Succ
  type _5 = _4#Succ
  type _6 = _5#Succ
  type _7 = _6#Succ
  type _8 = _7#Succ
  type _9 = _8#Succ
  type _10 = _9#Succ
  type _11 = _10#Succ
  type _12 = _11#Succ
  type _13 = _12#Succ
  type _14 = _13#Succ
  type _15 = _14#Succ
  type _16 = _15#Succ

  type x = _0
  type y = _1
  type z = _2
  type w = _3

  type +[N1 <: CInt, N2 <: CInt] = N1#Add[N2]
  type -[N1 <: CInt, N2 <: CInt] = N1#Sub[N2]

  type GE[X <: CInt, Y <: CInt] = (X - Y)#IsNeg#Not
  type GT[X <: CInt, Y <: CInt] = (X - (Y + _1))#IsNeg#Not
  type LE[X <: CInt, Y <: CInt] = (Y - X)#IsNeg#Not
  type LT[X <: CInt, Y <: CInt] = (Y - (X + _1))#IsNeg#Not

  type RangeCC[X <: CInt, Start <: CInt, End <: CInt] = GE[X, Start] && LE[X, End]
  type RangeOO[X <: CInt, Start <: CInt, End <: CInt] = GT[X, Start] && LT[X, End]
  type RangeCO[X <: CInt, Start <: CInt, End <: CInt] = GE[X, Start] && LT[X, End]
  type RangeOC[X <: CInt, Start <: CInt, End <: CInt] = GT[X, Start] && LE[X, End]

  // [Michid] with my own touch to work with Church Encoding for Positives and Negatives
  abstract class Rep[T] {
    def eval: Int
  }

  implicit def toRep0(k: _0) = new Rep[_0] {
    def eval = 0
  }

  implicit def toRepN[T <: CPos](k: CSucc[T])(implicit f: T => Rep[T]) = new Rep[CSucc[T]] {
    def eval = f(null.asInstanceOf[T]).eval + 1
  }

  implicit def toRepNegN[T <: CNeg](k: CPred[T])(implicit f: T => Rep[T]) = new Rep[CPred[T]] {
    def eval = f(null.asInstanceOf[T]).eval - 1
  }

  def depth[T <% Rep[T]](m: T): Int = m.eval

  // [/Michid]

  class Vector[T, X <: CInt](implicit f: X => Rep[X], implicit val man: ClassTag[T]) {
    private[this] val a = new Array[T](depth(null.asInstanceOf[X]))

    @inline def array: Array[T] = a

    @inline def size = a.size

    @inline final def apply(i: Int): T = array(i)

    @inline final def update(i: Int, x: T): Unit = array(i) = x

    @inline final def update[Y <: CInt](x: T)(implicit t: IsTrue[LT[Y, X] && GE[Y, _0]], f: Y => Rep[Y]): Unit = array(depth(null.asInstanceOf[Y])) = x

    override def toString(): String = "Vector[" + a.mkString(", ") + "]"

    def concatenate[Y <: CInt](v2: Vector[T, Y])(implicit f: X + Y => Rep[X + Y]): Vector[T, X + Y] = {
      val vec = new Vector[T, X + Y]
      for (i <- 0 until size) {
        vec(i) = this(i)
      }

      for (i <- 0 until v2.size) {
        vec(i + size) = v2(i)
      }

      vec
    }
  }

  def main(args: Array[String]): Unit = {
    println(depth(new _0))
    println(depth(new _3))
//    println(depth(new _2#Neg))

    val vector = new Vector[Float, _2]
    //	vector.update[_1#Neg](1.0f) // Fails at compile time!
    vector.update[x](2.0f) // Safe update
    vector.update[y](2.0f) // Safe update
    //	vector.update[z](1.0f) // Fails at compile time!

    val vector2 = new Vector[Float, _3]
    vector2(0) = 3.0f
    vector2(1) = 4.0f
    vector2(2) = 5.0f
    //	vector2(3) = 5.0f // Doesn't fail at compile time!

    val vector3 = vector concatenate vector2
    vector3.update[x](-99.0f)
    vector3.update[_4](-99.0f)
    //	vector3.update[_5](4.0f) // Fails at compile time!

    println("vector: " + vector)
    println("vector2: " + vector2)
    println("vector3: " + vector3)
  }
}
