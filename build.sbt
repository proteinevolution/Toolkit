name := """Toolkit_scala"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
//lazy val `toolkitscala` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  filters,
  cache,
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.13",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.13" % "test",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
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
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

