package net.chwthewke.freegen

import cats._
import cats.data._
import cats.state._

import scala.util.Random

object g4 {

  sealed trait Error
  case object FilterFailed extends Error
  case class Retried( count : Int, e : Error ) extends Error

  case class Params( size : Int, maxRetries : Int )

  case class GenState( retries : Int, random : Random )

  type GenComp[A] = XorT[ReaderT[WriterT[State[GenState, ?], Stream[String], ?], Params, ?], Error, A]

  sealed trait Gen[A] {
    def run : GenComp[A]
  }

  case class Emit[A]( f : ( Int => Array[Byte] ) => A ) extends Gen[A] {
    override def run : GenComp[A] =

  }


}
