addSbtPlugin("com.hanhuy.sbt" % "android-sdk-plugin" % "1.2.11")

// sbt-idea snapshot rep

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.7.0-SNAPSHOT")
