val akkaVersion = "2.4.11"
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
val reactiveMongoVersion = "0.12-RC5"

val commonDeps = Seq(ws, filters, cache,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.sanoma.cda" %% "maxmind-geoip2-scala" % "1.5.1",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.play" %% "play-mailer" % playMailerVersion,  // Mailer Plugin: https://github.com/playframework/play-mailer
  "net.codingwell" %% "scala-guice" % scalaGuiceVersion,
  "com.github.pathikrit" %% "better-files" % betterfilesVersion,
  "org.mindrot" % "jbcrypt" % bcryptVersion,
  "com.evojam" %% "play-elastic4s" % "0.3.1",
  "org.reactivemongo" %% "play2-reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "reactivemongo-play-json" % reactiveMongoVersion,
  "co.fs2" %% "fs2-core" % "0.9.0-RC2",
  "org.scalaz" %% "scalaz-core" % "7.2.5",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "org.clapper" %% "classutil" % "1.0.11",
  "com.beachape" %% "enumeratum" % "1.4.13",
  "com.beachape" %% "enumeratum-play" % "1.4.13",
  "com.beachape" %% "enumeratum-play-json" % "1.4.13"
)

/*
 * Settings which apply to all modules of this application
 */
val commonSettings = Seq(
  version := "0.0.0",
  scalaVersion := "2.11.8",
  logLevel := Level.Warn,
  dependencyOverrides ++= Set("org.webjars" % "jquery" % jqueryVersion,
                              "com.typesafe.akka" %% "akka-actor" % akkaVersion))

lazy val metadata = List(
  organization := "ebio.tuebingen.mpg.de",
  developers   := List(
    Developer("JoKuebler", "Jonas Kuebler", "jkuebler@tuebingen.mpg.de", url("https://github.com/JoKuebler")),
    Developer("zy4", "Seung-Zin Nam", "seungzin.nam@tuebingen.mpg.de", url("https://github.com/zy4")),
    Developer("davidrau", "David Rau", "drau@tuebingen.mpg.de", url("https://github.com/davidmrau")),
    Developer("anjestephens", "Andrew Jesse Stephens", "astephens@tuebingen.mpg.de", url("https://github.com/anjestephens")),
    Developer("lkszmmn", "Lukas Zimmermann", "lukas.zimmermann@tuebingen.mpg.de", url("https://github.com/lkszmn"))
  )
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, SbtWeb)
  .settings(
    commonSettings,
    name := "mpi-toolkit",
    libraryDependencies ++= (commonDeps ++ Seq(
      "org.webjars" %% "webjars-play" % "2.5.0-3",
      "org.webjars" % "jquery" % jqueryVersion,
      "org.webjars.bower" % "jquery.lazyload" % jqueryLazyloadVersion,
      "org.webjars" % "jquery-ui" % jqueryUIVersion,
      "org.webjars" % "foundation" % foundationVersion,
      "org.webjars.bower" % "fastclick" % "1.0.6",
      "org.webjars.npm" % "mithril" % mithrilVersion,
      "org.webjars.bower" % "d3" % d3Version,
      "org.webjars.bower" % "slick-carousel" % "1.6.0",
      "org.webjars.npm" % "codemirror-minified" % "5.15.2",
      "org.webjars.npm" % "reformat.js" % "0.0.8",
      "org.webjars" % "dropzone" % "4.3.0",
      "org.webjars.bower" % "clipboard" % "1.5.10",
      "org.webjars" % "linkurious.js" % "1.5.1",
      "org.webjars.bower" % "tinymce" % "4.4.1",
      "org.webjars" % "highcharts" % highchartsVersion)),
    pipelineStages := Seq.empty,
    sassOptions in Assets ++= Seq("--compass", "-r", "compass"),
    sassOptions in Assets ++= Seq("--cache-location", "target/web/sass/.sass-cache")
  )


ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)


fork in run := false

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
