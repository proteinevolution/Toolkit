name := """Toolkit_scala"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)


scalaVersion := "2.11.7"

logLevel := Level.Warn

libraryDependencies ++= Seq(
  ws,
  filters,
  cache,
  evolutions,
  "org.webjars" %% "webjars-play" % "2.4.0-2",
  "org.webjars" % "jquery" % "2.2.1",
  "org.webjars" % "foundation" % "6.2.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "org.scalaz" %% "scalaz-core" % "7.2.1"
)

dependencyOverrides += "org.webjars" % "jquery" % "2.2.1"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
/*
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
*/

// Sass compiler options
sassOptions in Assets ++= Seq("--compass", "-r", "compass")
sassOptions in Assets ++= Seq("--cache-location", "target/web/sass/.sass-cache")

routesGenerator := InjectedRoutesGenerator


scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-optimise",
  "-explaintypes",
  "-encoding", "UTF-8",
  "-Xlint"
)

//fork in run := true
