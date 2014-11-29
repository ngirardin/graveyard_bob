import android.Keys._

android.Plugin.androidBuild

name := "ioiomastercontrol"

scalaVersion := "2.11.4"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
)

libraryDependencies += "org.scaloid" %% "scaloid" % "3.5-10" withSources() withJavadoc()

// Needs to be built locally according to https://github.com/pocorall/scaloid/issues/82
// Can't get the 3.6.1 version to build so using 3.6
libraryDependencies += "org.scaloid" %% "scaloid-support-v4" % "3.5-10"

// Used by Fragments
libraryDependencies += "com.android.support" % "support-v4" % "19.1.0"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.6"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
