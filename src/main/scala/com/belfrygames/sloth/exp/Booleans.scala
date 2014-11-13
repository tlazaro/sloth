package com.belfrygames.sloth.exp

object Booleans {

  import Utils._

  sealed trait Bool {
    type And[B <: Bool] <: Bool
    type Or[B <: Bool] <: Bool
    type Not <: Bool
    type If[IfTrue, IfFalse]
    type If2[T, IfTrue <: T, IfFalse <: T] <: T

    type Eq[B <: Bool] <: Bool
    type LT[B <: Bool] <: Bool
    type LTE[B <: Bool] <: Bool
    type GT[B <: Bool] <: Bool
    type GTE[B <: Bool] <: Bool
  }

  final class True extends Bool {
    type And[B <: Bool] = B
    type Or[B <: Bool] = True
    type Not = False
    type If[IfTrue, IfFalse] = IfTrue
    type If2[T, IfTrue <: T, IfFalse <: T] = IfTrue

    type Eq[B <: Bool] = B
    type LT[B <: Bool] = False
    type LTE[B <: Bool] = B
    type GT[B <: Bool] = B#Not
    type GTE[B <: Bool] = True
  }

  val True = new True

  final class False extends Bool {
    type And[B <: Bool] = False
    type Or[B <: Bool] = B
    type Not = True
    type If[IfTrue, IfFalse] = IfFalse
    type If2[T, IfTrue <: T, IfFalse <: T] = IfFalse

    type Eq[B <: Bool] = B#Not
    type LT[B <: Bool] = B
    type LTE[B <: Bool] = True
    type GT[B <: Bool] = False
    type GTE[B <: Bool] = B#Not
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
