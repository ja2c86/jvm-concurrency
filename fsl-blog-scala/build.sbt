
scalaVersion := "2.13.10"

name := "fsl-blog-scala"
organization := "co.fullstacklabs"
version := "1.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.4.8",
  "com.typesafe.akka" %% "akka-actor" % "2.8.0",
  "org.apache.commons" % "commons-lang3" % "3.12.0"
)
