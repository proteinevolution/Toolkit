import play.sbt.PlayImport._
import sbt._

object Dependencies {

  lazy val akkaVersion   = "2.5.6"
  lazy val scalazVersion = "7.2.16"

  lazy val commonDeps = Seq(
    ws,
    filters,
    ehcache,
    guice,
    "com.typesafe.akka"    %% "akka-actor"           % akkaVersion,
    "com.tgf.pizza"       %% "maxmind-geoip2-scala" % "1.5.5",
    "com.typesafe.akka"    %% "akka-cluster"         % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-tools"   % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka"    %% "akka-slf4j"           % akkaVersion,
    "com.typesafe.akka"    %% "akka-stream"          % akkaVersion,
    "com.typesafe.akka"    %% "akka-persistence"     % akkaVersion,
    "com.typesafe.play"    %% "play-mailer"          % "6.0.1",
    "com.typesafe.play"    %% "play-mailer-guice"    % "6.0.1",
    "com.github.pathikrit" %% "better-files"         % "2.17.1",
    "org.mindrot"          % "jbcrypt"               % "0.3m",
    "org.reactivemongo"    %% "play2-reactivemongo"  % "0.12.6-play26",
    "co.fs2"               %% "fs2-core"             % "0.9.2",
    "org.scalaz"           %% "scalaz-core"          % scalazVersion,
    "org.scalaz"           %% "scalaz-concurrent"    % scalazVersion,
    "com.chuusai"          %% "shapeless"            % "2.3.2",
    "com.lihaoyi"          %% "fastparse"            % "0.4.2",
    "com.vmunier"          %% "scalajs-scripts"      % "1.1.1",
    "org.typelevel"        %% "cats"                 % "0.9.0",
    "com.mohiva"           %% "play-html-compressor" % "0.7.1",
    "com.typesafe.play"    %% "play-json"            % "2.6.3",
    "com.github.dfabulich" % "sitemapgen4j"          % "1.0.6",
    "org.specs2"           %% "specs2-core"          % "4.0.0" % "test"
  )

}
