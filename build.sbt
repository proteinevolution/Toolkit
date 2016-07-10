val akkaVersion = "2.4.4"
val jqueryVersion = "2.2.4"
val jqueryLazyloadVersion = "1.9.7"
val jqueryUIVersion = "1.11.4"
val foundationVersion = "6.2.3"
val mithrilVersion = "0.2.5"
val betterfilesVersion = "2.15.0"
val bcryptVersion = "0.3m"
val highchartsVersion = "4.2.4"
val d3Version = "3.5.16"
val scalaGuiceVersion = "4.0.1"
val playMailerVersion = "5.0.0-M1"

val commonDeps = Seq(ws,  filters, cache, evolutions,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.play" %% "play-mailer" % playMailerVersion,  // Mailer Plugin: https://github.com/playframework/play-mailer
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "net.codingwell" %% "scala-guice" % scalaGuiceVersion,
  "com.github.pathikrit" %% "better-files" % betterfilesVersion,
  "org.mindrot" % "jbcrypt" % bcryptVersion,
  "com.evojam" %% "play-elastic4s" % "0.3.0",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"
)

/*
 * Settings which apply to all modules of this application
 */
val commonSettings = Seq(
  organization := "your.organization",
  version := "2.5.4",
  scalaVersion := "2.11.8",
  logLevel := Level.Warn,
  dependencyOverrides ++= Set("org.webjars" % "jquery" % jqueryVersion,
                              "com.typesafe.akka" %% "akka-actor" % akkaVersion)
)



lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, SbtWeb)
  .settings(
    commonSettings,
    name := "mpi-toolkit",
    libraryDependencies ++= (commonDeps ++ Seq(
      "org.webjars" %% "webjars-play" % "2.5.0",
      "org.webjars" % "jquery" % jqueryVersion,
      "org.webjars.bower" % "jquery.lazyload" % jqueryLazyloadVersion,
      "org.webjars" % "jquery-ui" % jqueryUIVersion,
      "org.webjars" % "foundation" % foundationVersion,
      "org.webjars" % "modernizr" % "2.8.3",
      "org.webjars.bower" % "fastclick" % "1.0.6",
      "org.json4s" %% "json4s-jackson" % "3.3.0",
      "org.scalaz" %% "scalaz-core" % "7.2.1",
      "org.webjars" % "mithril" % mithrilVersion,
      "org.webjars.bower" % "d3" % d3Version,
      "org.webjars.bower" % "slick-carousel" % "1.6.0",
      "org.webjars.npm" % "codemirror-minified" % "5.15.2",
      "org.webjars" % "dropzone" % "4.3.0",
      "org.webjars.bower" % "clipboard" % "1.5.10",
      "org.webjars" % "linkurious.js" % "1.5.1",
      "org.webjars" % "highcharts" % highchartsVersion)),
    pipelineStages := Seq.empty,
    sassOptions in Assets ++= Seq("--compass", "-r", "compass"),
    sassOptions in Assets ++= Seq("--cache-location", "target/web/sass/.sass-cache")
  )
  .dependsOn(api)



lazy val jobDB = (project in file("jobDB"))
  .settings(
    commonSettings,
    name := "mpi-toolkit-jobDB",
    libraryDependencies ++= commonDeps
  ).dependsOn(api)



lazy val api = (project in file("api"))
  .settings(
    commonSettings,
    name := "mpi-toolkit-api",
    libraryDependencies ++= commonDeps
  )

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


/*
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
*/


scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-optimise",
  "-explaintypes",
  "-encoding", "UTF-8",
  "-Xlint",
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code"
)
