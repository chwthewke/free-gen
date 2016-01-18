package net.chwthewke.freegen

/**
 * @author Chewie
 */
object g2 {

  sealed trait GenF[A]

  case class Random[A]( n : Int, k : Array[Byte] => A )

}
