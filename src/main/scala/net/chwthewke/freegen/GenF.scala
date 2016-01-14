package net.chwthewke.freegen

import cats.Functor
import scala.util.Random
import cats.free.Free
import cats.state.StateT
import cats.Monad

// TODO see how other peeps organize that stuff
// tbh, also see how they make it work :D

sealed trait GenF[A] {
  def map[B]( f : A => B ) : GenF[B]
}

class RandomF[A]( c : Int, v : Array[Byte] => A ) extends GenF[A] {
  override def map[B]( f : A => B ) : GenF[B] =
    new RandomF[B]( c, f compose v )
}

object GenF extends GenFInstances {

  def randomBytes( count : Int ) : GenF[Array[Byte]] = new RandomF( count, identity )

  val randomByte : GenF[Byte] = new RandomF( 1, _( 0 ) )

  val randomInt : GenF[Int] = new RandomF( 4,
    _.foldLeft( 0 ) { case ( i, b ) => i << 8 | ( b & 0xff ) } )

  val randomLong : GenF[Long] = new RandomF( 8,
    _.foldLeft( 0L ) { case ( l, b ) => l << 8 | ( b & 0xff : Long ) } )

  val randomFloat : GenF[Float] = randomInt.map( java.lang.Float.intBitsToFloat _ )

  val randomDouble : GenF[Double] = randomLong.map( java.lang.Double.longBitsToDouble _ )

}

trait GenFInstances {
  implicit val GenFunctor : Functor[GenF] = new Functor[GenF] {
    override def map[A, B]( fa : GenF[A] )( f : A => B ) : GenF[B] = fa.map( f )
  }
}

case class GenState( random : Random )

object RunGen {
  def runGen[M[_] : Monad, A]( g : Free[GenF, A] ) : StateT[M, GenState, A] = ???
}
