package net.chwthewke.freegen

import cats._
import cats.arrow._
import cats.data._
import cats.implicits._
import java.util.Random

class RandomGen[A]( val run : RandomGen.Params => RandomGen.Result[A] ) extends AnyVal

object RandomGen {

  case class Params( random : Random )
  type Result[A] = Xor[String, A]

  implicit val runGenMonad : Monad[RandomGen] = new Monad[RandomGen] {
    def pure[A]( x : A ) : RandomGen[A] = new RandomGen( _ => x.right )

    def flatMap[A, B]( fa : RandomGen[A] )( f : A => RandomGen[B] ) : RandomGen[B] = new RandomGen( p =>
      fa.run( p ).flatMap( a => f( a ).run( p ) )
    )

  }

  val transform : GenF ~> RandomGen = new ( GenF ~> RandomGen ) {
    override def apply[A]( fa : GenF[A] ) : RandomGen[A] = fa match {
      case RandomF( c, v ) => new RandomGen( p => {
        val bytes = Array.ofDim[Byte]( c )
        p.random.nextBytes( bytes )
        v( bytes ).right
      } )
    }
  }

}
