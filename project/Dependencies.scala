import play.sbt.PlayImport._
import sbt._

object Dependencies {

  val akkaVersion = "2.5.25"
  val catsV       = "2.0.0"
  val circeV      = "0.11.1"

  lazy val commonDeps: Seq[ModuleID] = Seq(
    ws,
    filters,
    ehcache,
    guice,
    "com.typesafe.akka"    %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster"         % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-tools"   % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka"    %% "akka-slf4j"           % akkaVersion,
    "com.typesafe.akka"    %% "akka-stream"          % akkaVersion,
    "com.typesafe.play"    %% "play-mailer"          % "7.0.1",
    "com.typesafe.play"    %% "play-mailer-guice"    % "7.0.0",
    "com.github.pathikrit" %% "better-files"         % "3.7.1",
    "org.mindrot"          % "jbcrypt"               % "0.3m",
    ("org.reactivemongo" %% "play2-reactivemongo" % "0.16.2-play27")
      .exclude("com.typesafe.akka", "*") // provided
      .exclude("com.typesafe.play", "*"), // provided
    ("org.reactivemongo" %% "reactivemongo-akkastream" % "0.16.2")
      .exclude("com.typesafe.akka", "*") // provided
      .exclude("com.typesafe.play", "*"), // provided
    "org.typelevel" %% "cats-core"            % catsV,
    "org.typelevel" %% "cats-effect"          % "1.3.1",
    "com.chuusai"   %% "shapeless"            % "2.3.3",
    "com.mohiva"    %% "play-html-compressor" % "0.7.1",
    "com.dripower"  %% "play-circe"           % "2711.0",
    "io.circe"      %% "circe-generic"        % circeV,
    "io.circe"      %% "circe-generic-extras" % circeV,
    "io.circe"      %% "circe-java8"          % circeV,
    ("io.circe" %% "circe-bson" % "0.3.1").exclude("org.reactivemongo", "*"), // provided by play2-reactivemongo
    "com.github.mpilquist" %% "simulacrum"    % "0.15.0",
    "com.github.mongobee"  % "mongobee"       % "0.13",
    "com.maxmind.geoip2"   % "geoip2"         % "2.12.0"
  )

  lazy val testDeps: Seq[ModuleID] = Seq(
    "com.typesafe.akka"        %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka"        %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"  % "4.0.1"     % Test,
    "org.mockito"              % "mockito-core"         % "2.11.0"    % Test,
    "com.softwaremill.macwire" %% "macros"              % "2.3.1"     % Test,
    "org.awaitility"           % "awaitility"           % "3.0.0"     % Test
  )

}
