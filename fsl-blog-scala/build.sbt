scalaVersion := "3.3.1"

name := "fsl-blog-scala"
organization := "co.fullstacklabs"
version := "1.0"

libraryDependencies ++= Seq(
  "com.softwaremill.ox" %%  "core"          % "0.0.14",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.8.0",
  "dev.zio"             %%  "zio"           % "2.0.18",
  "org.apache.commons"  %   "commons-lang3" % "3.12.0",
  "org.typelevel"       %%  "cats-effect"   % "3.5.0"
)
