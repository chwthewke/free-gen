import sbt._
import sbt.Keys._
import com.typesafe.sbt.SbtScalariform._
import com.typesafe.sbteclipse.core.EclipsePlugin._
import scalariform.formatter.preferences._
import scoverage.ScoverageSbtPlugin
import sbtbuildinfo.Plugin._

object FreegenBuild extends Build {

  object Dependencies {

    val scalatest = "org.scalatest" %% "scalatest" % "2.2.6" % "test" withSources () withJavadoc ()

    val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.5" % "test" withSources () withJavadoc ()

    val cats = "org.spire-math" %% "cats" % "0.3.0" withSources () withJavadoc ()
  }

  override def settings = super.settings :+ ( EclipseKeys.skipParents in ThisBuild := false )

  lazy val freegenScalariformSettings = scalariformSettings ++ Seq(
    ScalariformKeys.preferences := defaultPreferences
      .setPreference( AlignSingleLineCaseStatements, true )
      .setPreference( SpaceBeforeColon, true )
      .setPreference( SpaceInsideParentheses, true )
  )

  lazy val sharedSettings =
    Seq(
      organization := "net.chwthewke",
      scalaVersion := "2.11.7" )

  lazy val freegenSettings =
    Defaults.coreDefaultSettings ++
      SbtBuildInfo.buildSettings( "net.chwthewke.freegen" ) ++
      SbtEclipse.buildSettings ++
      freegenScalariformSettings ++
      sharedSettings ++
      Seq(
        libraryDependencies ++= Seq(
          Dependencies.cats,
          Dependencies.scalatest,
          Dependencies.scalacheck ),
        scalacOptions ++= Seq(
          "-feature",
          "-deprecation",
          "-language:higherKinds" ),
        unmanagedSourceDirectories in Compile := ( scalaSource in Compile ).value :: Nil,
        unmanagedSourceDirectories in Test := ( scalaSource in Test ).value :: Nil
      )

  lazy val freegen = Project(
    id = "free-gen",
    base = file( "." ),
    settings = freegenSettings ++
      Seq(
        name := "free-gen",
        mainClass := Some( "net.chwthewke.freegen.Main" ),
        initialCommands := """|import net.chwthewke.freegen._""".stripMargin
      )
  )
}
