package net.chwthewke

import cats.free._

package object freegen {
  type Gen[A] = Free[GenF, A]
}
