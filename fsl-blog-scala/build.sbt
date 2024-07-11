scalaVersion := "3.3.1"

name := "fsl-blog-scala"
organization := "co.fullstacklabs"
version := "1.0"

libraryDependencies ++= Seq(
  "io.getkyo"           %%  "kyo-core"      % "0.8.7",
  "io.getkyo"           %%  "kyo-direct"    % "0.8.7",
  "com.softwaremill.ox" %%  "core"          % "0.0.21",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.9.0-M2",
  "dev.zio"             %%  "zio"           % "2.0.21",
  "org.apache.commons"  %   "commons-lang3" % "3.14.0",
  "org.typelevel"       %%  "cats-effect"   % "3.5.0"
)
