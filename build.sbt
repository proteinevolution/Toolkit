name := """Toolkit_scala"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
//lazy val `toolkitscala` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws, // Play's web services module
  filters,
  cache,
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.13",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.13" % "test"
)

/*
libraryDependencies ++= Seq(jdbc , cache , filters, ws,
  "org.webjars" % "jquery" % "2.1.3",
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.1",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.1" % "test")
*/

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

routesGenerator := InjectedRoutesGenerator

//fork in run := true


