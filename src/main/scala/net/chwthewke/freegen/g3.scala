package net.chwthewke.freegen

import cats._, implicits._
import cats.data._
import scala.util.Random
import cats.{ MonadError, Monad }

/**
 * @author Chewie
 */
object g3 {

  case class Params( size : Int, maxRetries : Int )

  case class GenState( p : Params, retries : Int, random : Random ) {
    private[freegen] def bytes( count : Int ) : Array[Byte] = {
      val array = Array.ofDim[Byte]( count )
      random.nextBytes( array )
      array
    }

    def size = p.size

    def maxRetries = p.maxRetries

    def resize( n : Int ) = copy( p = p.copy( size = n ) )
  }

  sealed trait Error
  case object FilterFailed extends Error
  case class Retried( count : Int, e : Error ) extends Error

  type Result[A] = Xor[Error, A]

  implicitly[MonadError[Result, Error]]

  sealed trait GenAst[A] {
    def run( s : GenState ) : Result[A]
  }

  implicit val genAstMonadError : MonadError[GenAst, Error] = new MonadError[GenAst, Error] {
    override def pure[A]( x : A ) : GenAst[A] = Emit( _ => x )

    override def flatMap[A, B]( fa : GenAst[A] )( f : ( A ) => GenAst[B] ) : GenAst[B] = new GenAst[B] {
      override def run( s : GenState ) : Result[B] =
        fa.run( s ).flatMap( a => f( a ).run( s ) )
    }

    override def raiseError[A]( e : Error ) : GenAst[A] = new GenAst[A] {
      override def run( s : GenState ) : Result[A] = e.left
    }

    override def handleErrorWith[A]( fa : GenAst[A] )( f : Error => GenAst[A] ) : GenAst[A] = new GenAst[A] {
      override def run( s : GenState ) : Result[A] =
        MonadError[Result, Error].handleErrorWith( fa.run( s ) )( ( e : Error ) => f( e ).run( s ) )
    }
  }

  case class Emit[A]( f : ( Int => Array[Byte] ) => A ) extends GenAst[A] {
    override def run( s : GenState ) : Result[A] =
      f( s.bytes ).right
  }

  case class Sized[A]( f : Int => GenAst[A] ) extends GenAst[A] {
    override def run( s : GenState ) : Result[A] =
      f( s.size ).run( s )
  }

  case class Resize[A]( n : Int, g : GenAst[A] ) extends GenAst[A] {
    override def run( s : GenState ) : Result[A] =
      g.run( s.resize( n ) )
  }

  case class Filter[A]( c : A => Boolean, g : GenAst[A] ) extends GenAst[A] {
    override def run( s : GenState ) : Result[A] = {
      g.run( s ).fold( _.left, a => if ( c( a ) ) a.right else FilterFailed.left )
    }
  }

}
