package net.chwthewke.freegen

import cats._, implicits._
import cats.data._
import scala.util.Random
import cats.{ MonadError, Monad }

/**
 * @author Chewie
 */
object g3 {

  case class Params( size : Int, retries : Int, random : Random ) {
    private[freegen] def bytes( count : Int ) : Array[Byte] = {
      val array = Array.ofDim[Byte]( count )
      random.nextBytes( array )
      array
    }
  }

  case class Error( message : String )

  type Result[A] = Xor[Error, A]

  implicitly[MonadError[Result, Error]]

  sealed trait GenAst[A] {
    def run( p : Params ) : Result[A]
  }

  implicit val genAstMonadError : MonadError[GenAst, String] = new MonadError[GenAst, String] {
    override def pure[A]( x : A ) : GenAst[A] = Emit( _ => x )

    override def flatMap[A, B]( fa : GenAst[A] )( f : ( A ) => GenAst[B] ) : GenAst[B] = new GenAst[B] {
      override def run( p : Params ) : Result[B] =
        fa.run( p ).flatMap( a => f( a ).run( p ) )
    }

    override def raiseError[A]( e : String ) : GenAst[A] = new GenAst[A] {
      override def run( p : Params ) : Result[A] = Error( e ).left
    }

    override def handleErrorWith[A]( fa : GenAst[A] )( f : ( String ) => GenAst[A] ) : GenAst[A] = new GenAst[A] {
      override def run( p : Params ) : Result[A] =
        MonadError[Result, Error].handleErrorWith( fa.run( p ) )( ( e : Error ) => f( e.message ).run( p ) )
    }
  }

  case class Emit[A]( f : ( Int => Array[Byte] ) => A ) extends GenAst[A] {
    override def run( p : Params ) : Result[A] =
      f( p.bytes ).right
  }

  case class Sized[A]( f : Int => GenAst[A] ) extends GenAst[A] {
    override def run( p : Params ) : Result[A] =
      f( p.size ).run( p )
  }

  case class Resize[A]( n : Int, g : GenAst[A] ) extends GenAst[A] {
    override def run( p : Params ) : Result[A] =
      g.run( p.copy( size = n ) )
  }

  case class Filter[A]( p : A => Boolean, g : GenAst[A] ) extends GenAst[A] {
    override def run( p : Params ) : Result[A] = {
      ???
    }
  }

}
