import android.Keys._

android.Plugin.androidBuild

name := "bob-client"

scalaVersion := "2.11.0"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-dontwarn scala.collection.mutable.**")

libraryDependencies += "org.scaloid" %% "scaloid" % "3.4-10"

//TODO use published lib
libraryDependencies += "org.scaloid" %% "scaloid-support-v4" % "3.4-10"

libraryDependencies += "com.android.support" % "support-v4" % "19.0.1"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies += "io.spray" %%  "spray-json" % "1.2.6"

scalacOptions in Compile += "-feature"

run <<= run in Android

install <<= install in Android
