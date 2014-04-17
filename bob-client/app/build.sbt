import android.Keys._

android.Plugin.androidBuild

name := "bob-client"

scalaVersion := "2.10.4"

scalacOptions in Compile += "-feature"

platformTarget in Android := "android-19"

libraryDependencies += "org.scaloid" %% "scaloid" % "3.2.1-8"
