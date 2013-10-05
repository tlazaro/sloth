import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ApplicationBuild extends Build {

  lazy val main = Project(
    id = "Sloth",
    base = file("."),
    settings = Defaults.defaultSettings ++ LWJGLPlugin.lwjglSettings ++ assemblySettings ++ Seq(
      scalaVersion := "2.10.3",
      version := "1.0-SNAPSHOT",
      mainClass in(Compile, run) := Some("com.belfrygames.sloth.Main"),
      mainClass in assembly := Some("com.belfrygames.sloth.Main"),
      scalacOptions += "-deprecation",
      packageBin in Compile <<= packageBin in Compile dependsOn LWJGLPlugin.lwjgl.manifestNatives
    )
  )

}