name := "circlet-example"
version := "0.1.0-SNAPSHOT"
organization := "com.markfeeney"
scalaVersion := "2.11.8"
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-unused-import"
)

cancelable in Global := true // ctrl-c doesn't exit shell

resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies ++= Seq(
  "com.markfeeney" % "circlet_2.11" % "0.2.0"
)
