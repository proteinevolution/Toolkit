import play.sbt.PlayImport._
import sbt._

object Dependencies {

  val akkaVersion = "2.5.19"
  val catsV       = "1.5.0"
  val circeV      = "0.11.1"

  lazy val commonDeps: Seq[ModuleID] = Seq(
    ws,
    filters,
    ehcache,
    guice,
    "com.typesafe.akka"    %% "akka-actor"               % akkaVersion,
    "com.tgf.pizza"        %% "maxmind-geoip2-scala"     % "1.5.7",
    "com.typesafe.akka"    %% "akka-cluster"             % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-tools"       % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-metrics"     % akkaVersion,
    "com.typesafe.akka"    %% "akka-slf4j"               % akkaVersion,
    "com.typesafe.akka"    %% "akka-stream"              % akkaVersion,
    "com.typesafe.play"    %% "play-mailer"              % "6.0.1",
    "com.typesafe.play"    %% "play-mailer-guice"        % "6.0.1",
    "com.github.pathikrit" %% "better-files"             % "3.5.0",
    "org.mindrot"          % "jbcrypt"                   % "0.3m",
    "org.reactivemongo"    %% "play2-reactivemongo"      % "0.16.0-play26",
    "org.reactivemongo"    %% "reactivemongo-akkastream" % "0.16.0",
    "org.typelevel"        %% "cats-core"                % catsV,
    "org.typelevel"        %% "cats-effect"              % "1.1.0",
    "com.chuusai"          %% "shapeless"                % "2.3.3",
    "org.atnos"            %% "eff"                      % "5.3.0",
    "org.tpolecat"         %% "atto-core"                % "0.6.4",
    "com.mohiva"           %% "play-html-compressor"     % "0.7.1",
    "com.dripower"         %% "play-circe-2611"          % "0",
    "io.circe"             %% "circe-generic"            % circeV,
    "io.circe"             %% "circe-generic-extras"     % circeV,
    "io.circe"             %% "circe-java8"              % circeV,
    "io.circe"             %% "circe-bson"               % "0.2.0",
    "com.github.mpilquist" %% "simulacrum"               % "0.12.0",
    "com.github.mongobee"  % "mongobee"                  % "0.13"
  )

  lazy val testDeps: Seq[ModuleID] = Seq(
    "com.typesafe.akka"        %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka"        %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"  % "3.1.2"     % Test,
    "org.mockito"              % "mockito-core"         % "2.11.0"    % Test,
    "com.softwaremill.macwire" %% "macros"              % "2.3.1"     % Test,
    "org.awaitility"           % "awaitility"           % "3.0.0"     % Test
  )

}
