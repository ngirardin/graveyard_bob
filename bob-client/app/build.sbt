import android.Keys._

android.Plugin.androidBuild

name := "bob-client"

scalacOptions in Compile += "-feature"

platformTarget in Android := "android-19"

libraryDependencies += "com.android.support" % "appcompat-v7" % "19.0.1"
