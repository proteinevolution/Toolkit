import play.sbt.PlayImport._
import sbt._

object Dependencies {

  val akkaVersion = "2.6.5"
  val catsV       = "2.1.0"
  val circeV      = "0.13.0"

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
    "com.typesafe.play"    %% "play-mailer"          % "8.0.1",
    "com.typesafe.play"    %% "play-mailer-guice"    % "8.0.1",
    "com.github.pathikrit" %% "better-files"         % "3.9.1",
    "org.mindrot"           % "jbcrypt"              % "0.4",
    ("org.reactivemongo"   %% "play2-reactivemongo"  % "0.20.12-play28-fix1")
      .exclude("com.typesafe.akka", "*")  // provided
      .exclude("com.typesafe.play", "*"), // provided
    ("org.reactivemongo" %% "reactivemongo-akkastream" % "0.20.12-fix1")
      .exclude("com.typesafe.akka", "*")  // provided
      .exclude("com.typesafe.play", "*"), // provided
    "org.typelevel"       %% "cats-core"            % catsV,
    "org.typelevel"       %% "cats-effect"          % "2.1.4",
    "com.chuusai"         %% "shapeless"            % "2.3.3",
    "com.dripower"        %% "play-circe"           % "2812.0",
    "io.circe"            %% "circe-generic"        % circeV,
    "io.circe"            %% "circe-generic-extras" % circeV,
    ("io.circe"           %% "circe-bson"           % "0.4.0").exclude("org.reactivemongo", "*"), // provided by play2-reactivemongo
    "org.typelevel"       %% "simulacrum"           % "1.0.0",
    "com.github.dalet-oss" % "mongobee"             % "1.0.4",
    "com.maxmind.geoip2"   % "geoip2"               % "2.13.0"
  )

  lazy val testDeps: Seq[ModuleID] = Seq(
    "com.typesafe.akka"        %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka"        %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"  % "5.1.0"     % Test,
    "org.mockito"               % "mockito-core"        % "3.5.5"     % Test,
    "com.softwaremill.macwire" %% "macros"              % "2.3.7"     % Test,
    "org.awaitility"            % "awaitility"          % "4.0.3"     % Test
  )

}
