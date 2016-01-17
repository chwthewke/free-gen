package net.chwthewke.freegen

import cats._
import cats.free._

sealed trait GenF[A] {
  def map[B]( f : A => B ) : GenF[B]
}

object GenF extends GenFFunctions {
  implicit val functor : Functor[GenF] = new Functor[GenF] {
    override def map[A, B]( fa : GenF[A] )( f : A => B ) : GenF[B] = fa.map( f )
  }
}

trait GenFFunctions {
  def randomBytes( count : Int ) : Gen[Array[Byte]] = Free.liftF( new RandomF( count, identity ) )

  val randomByte : Gen[Byte] = Free.liftF( new RandomF( 1, _( 0 ) ) )

  val randomInt : Gen[Int] = Free.liftF( new RandomF( 4,
    _.foldLeft( 0 ) { case ( i, b ) => i << 8 | ( b & 0xff ) } ) )

  val randomLong : Gen[Long] = Free.liftF( new RandomF( 8,
    _.foldLeft( 0L ) { case ( l, b ) => l << 8 | ( b & 0xff : Long ) } ) )

  val randomFloat : Gen[Float] = randomInt.map( java.lang.Float.intBitsToFloat _ )

  val randomDouble : Gen[Double] = randomLong.map( java.lang.Double.longBitsToDouble _ )

}

case class RandomF[A]( c : Int, v : Array[Byte] => A ) extends GenF[A] {
  override def map[B]( f : A => B ) : GenF[B] =
    new RandomF[B]( c, f compose v )
}

