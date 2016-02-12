name := """Toolkit_scala"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

logLevel := Level.Warn

/*
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
*/

libraryDependencies ++= Seq(
  ws,
  filters,
  cache,
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars.bower" % "lodash" % "4.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "flot" % "0.8.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
  //"com.typesafe.akka" %% "akka-testkit" % "2.4.1" % "test",
)

routesGenerator := InjectedRoutesGenerator
//fork in run := true

