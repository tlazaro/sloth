package com.belfrygames.sloth.exp

import com.belfrygames.sloth.exp.Church._
import scala.math.Numeric._
import scala.math.Fractional

object Units {
  case class MKS[M<:CInt, K<:CInt, S<:CInt, T : Fractional](value : T) {
	val n = implicitly[Fractional[T]]
    def +(that:MKS[M,K,S,T]) = MKS[M,K,S,T](n.mkNumericOps(this.value) + that.value)
    def -(that:MKS[M,K,S,T]) = MKS[M,K,S,T](n.mkNumericOps(this.value) - that.value)
    def *[M2<:CInt,K2<:CInt,S2<:CInt](that:MKS[M2,K2,S2,T]) = MKS[M+M2,K+K2,S+S2,T](n.mkNumericOps(this.value) * that.value)
    def /[M2<:CInt,K2<:CInt,S2<:CInt](that:MKS[M2,K2,S2,T]) = MKS[M-M2,K-K2,S-S2,T](n.mkNumericOps(this.value) / that.value)

    def ==(that:MKS[M,K,S,T]) = n.equiv(this.value, that.value)
    def !=(that:MKS[M,K,S,T]) = !n.equiv(this.value , that.value)
    def > (that:MKS[M,K,S,T]) = n.gt(this.value, that.value)
    def >=(that:MKS[M,K,S,T]) = n.gteq(this.value, that.value)
    def < (that:MKS[M,K,S,T]) = n.lt(this.value, that.value)
    def <=(that:MKS[M,K,S,T]) = n.lteq(this.value, that.value)
  }

  type Scalar[T] = MKS[_0,_0,_0, T]     //scalar
  type Length[T] = MKS[_1,_0,_0, T]     //meter
  type Area[T] = MKS[_2,_0,_0, T]       //square meter
  type Volume[T] = MKS[_3,_0,_0, T]     //cubic meter, or kiloliter
  type Mass[T] = MKS[_0,_1,_0, T]       //kilogram
  type Time[T] = MKS[_0,_0,_1, T]       //second
  type Frequency[T] = MKS[_0,_0,_1#Neg, T] //hertz
  type Speed[T] = MKS[_1,_0,_1#Neg, T]  //meter per second
  type Acceleration[T] = MKS[_1,_0,_2#Neg, T] //meter per second-squared
  type Force[T] = MKS[_1,_1,_2#Neg, T]  //newton
  type Energy[T] = MKS[_2,_1,_2#Neg, T] //joule
  type Power[T] = MKS[_2,_1,_3#Neg, T]  //watt

  implicit def toScalar(x : Float) = new Scalar(x)
  implicit def toScalar(x : Double) = new Scalar(x)

  implicit def toScalarT[T : Fractional : ClassManifest](x : Double) : Scalar[T] = {
	val m = implicitly[ClassManifest[T]]
	if (Float.getClass == m.erasure)
	  new Scalar(x.toFloat.asInstanceOf[T])
	else
	  new Scalar(x.asInstanceOf[T])
  }

  implicit def toScalarT[T : Fractional : ClassManifest](x : Float) : Scalar[T] = {
	val m = implicitly[ClassManifest[T]]
	if (Float.getClass == m.erasure)
	  new Scalar(x.asInstanceOf[T])
	else
	  new Scalar(x.toDouble.asInstanceOf[T])
  }

  def circleArea[T : Fractional : ClassManifest](r : Length[T]) : Area[T] = r * r * java.lang.Math.PI
  def sphereSurfaceArea[T : Fractional : ClassManifest](r : Length[T]) : Area[T] = r * r * (4. * java.lang.Math.PI)
  def sphereVolume[T : Fractional : ClassManifest](r : Length[T]) : Volume[T] = r * r * r * ((4./3.) * java.lang.Math.PI)

  val x = new Length(5.0f)
  val y = new Length(6.0f)
  val z = new Length(2.0f)
  val a : Area[Float] = x * y
  val v : Volume[Float] = a * z
  circleArea(x)
  sphereVolume(y)
//  sphereVolume(a)         //type error
}
