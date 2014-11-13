package com.belfrygames.sloth.exp

import Booleans._

object BooleansTests {
  // Boolean LT check
  val f_LT_f: False#LT[False] = False
  val f_LT_t: False#LT[True] = True
  val t_LT_f: True#LT[False] = False
  val t_LT_t: True#LT[True] = False

  // Boolean LTE check
  val f_LTE_f: False#LTE[False] = True
  val f_LTE_t: False#LTE[True] = True
  val t_LTE_f: True#LTE[False] = False
  val t_LTE_t: True#LTE[True] = True

  // Boolean GT check
  val f_GT_f: False#GT[False] = False
  val f_GT_t: False#GT[True] = False
  val t_GT_f: True#GT[False] = True
  val t_GT_t: True#GT[True] = False

  // Boolean GTE check
  val f_GTE_f: False#GTE[False] = True
  val f_GTE_t: False#GTE[True] = False
  val t_GTE_f: True#GTE[False] = True
  val t_GTE_t: True#GTE[True] = True

  // Boolean Eq check
  val f_Eq_f: False#Eq[False] = True
  val f_Eq_t: False#Eq[True] = False
  val t_Eq_f: True#Eq[False] = False
  val t_Eq_t: True#Eq[True] = True

  // Boolean Or check
  val f_Or_f: False#Or[False] = False
  val f_Or_t: False#Or[True] = True
  val t_Or_f: True#Or[False] = True
  val t_Or_t: True#Or[True] = True

  // Boolean And check
  val f_And_f: False#And[False] = False
  val f_And_t: False#And[True] = False
  val t_And_f: True#And[False] = False
  val t_And_t: True#And[True] = True
}

object BitwiseTypes {

  trait Bits2 {
    type _1 <: Bool
    type _0 <: Bool

    type And[a1 <: Bool, a0 <: Bool] <: Bits2
    type Or[a1 <: Bool, a0 <: Bool] <: Bits2
    type Eq[o <: Bits2] <: Bool

    type LT[o <: Bits2] <: Bool
    type GT[o <: Bits2] <: Bool

    type Prev <: Bits2
    type lshift[o <: Bits2] <: Bits2
  }

  class Bit2Impl[b1 <: Bool, b0 <: Bool] extends Bits2 {
    type _1 = b1
    type _0 = b0

    type And[a1 <: Bool, a0 <: Bool] = Bit2Impl[a1 && b1, a0 && b0]
    type Or[a1 <: Bool, a0 <: Bool] = Bit2Impl[a1 || b1, a0 || b0]
    type Eq[O <: Bits2] = O#_1#Eq[_1] && O#_0#Eq[_0]

    type LT[o <: Bits2] = _1#LT[o#_1] || (_1#Eq[o#_1] && _0#LT[o#_0])
    type GT[o <: Bits2] = _1#GT[o#_1] || (_1#Eq[o#_1] && _0#GT[o#_0])

    type Prev = _0#If[Bit2Impl[b1, False], _1#If[Bit2Impl[False, b0], Bit2Impl[b1#Not, b0#Not]]]

    type lshift[o <: Bits2] = (o#_1#Eq[False] && o#_0#Eq[False])#If[Bit2Impl[b1, b0], Bit2Impl[b0, False]#lshift[o#Prev]]
  }

  type b00 = Bit2Impl[False, False]
  type b01 = Bit2Impl[False, True]
  type b10 = Bit2Impl[True, False]
  type b11 = Bit2Impl[True, True]

  val b00 = new Bit2Impl[False, False]
  val b01 = new Bit2Impl[False, True]
  val b10 = new Bit2Impl[True, False]
  val b11 = new Bit2Impl[True, True]

  type x00 = b00
  type x01 = b01
  type x02 = b10
  type x03 = b11
}

object BitwiseTypesTests {

  import BitwiseTypes._

  val a: Bit2Impl[True, False]#Eq[Bit2Impl[True, False]#Or[False, False]] = True
  val b: Bit2Impl[True, False]#LT[Bit2Impl[True, True]] = True

  // Bits2 lshift
  val lshift0_0: b00#lshift[x00] = b00
  val lshift0_1: b00#lshift[x01] = b00
  val lshift0_2: b00#lshift[x02] = b00
  val lshift0_3: b00#lshift[x03] = b00

  val lshift1_0: b01#lshift[x00] = b01
  val lshift1_1: b01#lshift[x01] = b10
  val lshift1_2: b01#lshift[x02] = b00
  val lshift1_3: b01#lshift[x03] = b00

  val lshift2_0: b10#lshift[x00] = b10
  val lshift2_1: b10#lshift[x01] = b00
  val lshift2_2: b10#lshift[x02] = b00
  val lshift2_3: b10#lshift[x03] = b00

  val lshift3_0: b11#lshift[x00] = b11
  val lshift3_1: b11#lshift[x01] = b10
  val lshift3_2: b11#lshift[x02] = b00
  val lshift3_3: b11#lshift[x03] = b00

  // Bits2 LT
  val lt0_0: x00#LT[x00] = False
  val lt0_1: x00#LT[x01] = True
  val lt0_2: x00#LT[x02] = True
  val lt0_3: x00#LT[x03] = True

  val lt1_0: x01#LT[x00] = False
  val lt1_1: x01#LT[x01] = False
  val lt1_2: x01#LT[x02] = True
  val lt1_3: x01#LT[x03] = True

  val lt2_0: x02#LT[x00] = False
  val lt2_1: x02#LT[x01] = False
  val lt2_2: x02#LT[x02] = False
  val lt2_3: x02#LT[x03] = True

  val lt3_0: x03#LT[x00] = False
  val lt3_1: x03#LT[x01] = False
  val lt3_2: x03#LT[x02] = False
  val lt3_3: x03#LT[x03] = False

  // Bits2 GT
  val gt0_0: x00#GT[x00] = False
  val gt0_1: x00#GT[x01] = False
  val gt0_2: x00#GT[x02] = False
  val gt0_3: x00#GT[x03] = False

  val gt1_0: x01#GT[x00] = True
  val gt1_1: x01#GT[x01] = False
  val gt1_2: x01#GT[x02] = False
  val gt1_3: x01#GT[x03] = False

  val gt2_0: x02#GT[x00] = True
  val gt2_1: x02#GT[x01] = True
  val gt2_2: x02#GT[x02] = False
  val gt2_3: x02#GT[x03] = False

  val gt3_0: x03#GT[x00] = True
  val gt3_1: x03#GT[x01] = True
  val gt3_2: x03#GT[x02] = True
  val gt3_3: x03#GT[x03] = False
}