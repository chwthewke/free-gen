package net.chwthewke.freegen

import cats._
import cats.arrow.NaturalTransformation
import cats.data._
import cats.free.Free
import cats.macros._
import cats.implicits._
import cats.state._
import scala.util.Random

// TODO see how other peeps organize that stuff
// tbh, also see how they make it work :D

sealed trait GenF[A] {
  def map[B]( f : A => B ) : GenF[B]
}

case class RandomF[A]( c : Int, v : Array[Byte] => A ) extends GenF[A] {
  override def map[B]( f : A => B ) : GenF[B] =
    new RandomF[B]( c, f compose v )
}

object GenF extends GenFInstances {

  type G[A] = Free[GenF, A]

  def randomBytes( count : Int ) : G[Array[Byte]] = Free.liftF( new RandomF( count, identity ) )

  val randomByte : G[Byte] = Free.liftF( new RandomF( 1, _( 0 ) ) )

  val randomInt : G[Int] = Free.liftF( new RandomF( 4,
    _.foldLeft( 0 ) { case ( i, b ) => i << 8 | ( b & 0xff ) } ) )

  val randomLong : G[Long] = Free.liftF( new RandomF( 8,
    _.foldLeft( 0L ) { case ( l, b ) => l << 8 | ( b & 0xff : Long ) } ) )

  val randomFloat : G[Float] = randomInt.map( java.lang.Float.intBitsToFloat _ )

  val randomDouble : G[Double] = randomLong.map( java.lang.Double.longBitsToDouble _ )

}

trait GenFInstances {
  implicit val GenFunctor : Functor[GenF] = new Functor[GenF] {
    override def map[A, B]( fa : GenF[A] )( f : A => B ) : GenF[B] = fa.map( f )
  }
}

class RunGen[A]( val run : RunGen.Params => RunGen.Result[A] ) extends AnyVal

object RunGen {
  case class Params( random : Random )
  type Result[A] = Xor[String, A]

  implicit val runGenMonad : Monad[RunGen] = new Monad[RunGen] {
    def pure[A]( x : A ) : RunGen[A] = new RunGen( _ => x.right )

    def flatMap[A, B]( fa : RunGen[A] )( f : A => RunGen[B] ) : RunGen[B] = new RunGen( p =>
      fa.run( p ).flatMap( a => f( a ).run( p ) )
    )

  }

  val transform : GenF ~> RunGen = new ( GenF ~> RunGen ) {
    override def apply[A]( fa : GenF[A] ) : RunGen[A] = fa match {
      case RandomF( c, v ) => new RunGen( p => {
        val bytes = Array.ofDim[Byte]( c )
        p.random.nextBytes( bytes )
        v( bytes ).right
      } )
    }
  }

}
