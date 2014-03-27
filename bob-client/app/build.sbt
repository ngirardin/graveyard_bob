import android.Keys._

android.Plugin.androidBuild

name := "bob-client"

scalacOptions in Compile += "-feature"

platformTarget in Android := "android-19"
