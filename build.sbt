name := "toolkitscala"

version := "1.0"

lazy val `toolkitscala` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , cache , ws , "org.webjars" % "jquery" % "2.1.3" )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

routesGenerator := InjectedRoutesGenerator
