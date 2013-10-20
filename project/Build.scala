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
      scalacOptions in Test ++= Seq("-Yrangepos"),
      packageBin in Compile <<= packageBin in Compile dependsOn LWJGLPlugin.lwjgl.manifestNatives,

      libraryDependencies ++= Seq(
        "org.specs2" %% "specs2" % "2.2.3" % "test",
        "com.github.axel22" %% "scalameter" % "0.3" % "test"
      ),

      testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),

      // Read here for optional dependencies:
      // http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies
      resolvers ++= Seq(
        "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
        "releases" at "http://oss.sonatype.org/content/repositories/releases"
      )
    )
  )
}