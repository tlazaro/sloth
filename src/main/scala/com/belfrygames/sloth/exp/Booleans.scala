package com.belfrygames.sloth.exp

object Booleans {

  import Utils._

  sealed trait Bool {
    type And[B <: Bool] <: Bool
    type Or[B <: Bool] <: Bool
    type Not <: Bool
    type If[IfTrue, IfFalse]
    type If2[T, IfTrue <: T, IfFalse <: T] <: T
  }

  final class True extends Bool {
    type And[B <: Bool] = B
    type Or[B <: Bool] = True
    type Not = False
    type If[IfTrue, IfFalse] = IfTrue
    type If2[T, IfTrue <: T, IfFalse <: T] = IfTrue
  }

  val True = new True

  final class False extends Bool {
    type And[B <: Bool] = False
    type Or[B <: Bool] = B
    type Not = True
    type If[IfTrue, IfFalse] = IfFalse
    type If2[T, IfTrue <: T, IfFalse <: T] = IfFalse
  }

  val False = new False

  type &&[B1 <: Bool, B2 <: Bool] = B1#And[B2]
  type ||[B1 <: Bool, B2 <: Bool] = B1#Or[B2]

  implicit val falseToBoolean = TypeToValue[False, Boolean](false)
  implicit val trueToBoolean = TypeToValue[True, Boolean](true)

  trait IfTrue[P >: True <: True, T] {
    type Type = T
  }

}
