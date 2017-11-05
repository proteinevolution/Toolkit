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
    "com.typesafe.akka"    %% "akka-actor"               % akkaVersion,
    "com.sanoma.cda"       %% "maxmind-geoip2-scala"     % "1.5.4",
    "com.typesafe.akka"    %% "akka-cluster"             % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-tools"       % akkaVersion,
    "com.typesafe.akka"    %% "akka-cluster-metrics"     % akkaVersion,
    "com.typesafe.akka"    %% "akka-slf4j"               % akkaVersion,
    "com.typesafe.akka"    %% "akka-stream"              % akkaVersion,
    "com.typesafe.akka"    %% "akka-persistence"         % akkaVersion,
    "com.typesafe.play"    %% "play-mailer"              % "6.0.1",
    "com.typesafe.play"    %% "play-mailer-guice"        % "6.0.1",
    "com.github.pathikrit" %% "better-files"             % "2.17.1",
    "org.mindrot"          % "jbcrypt"                   % "0.3m",
    "org.reactivemongo"    %% "play2-reactivemongo"      % "0.12.7-play26",
    "org.reactivemongo"    %% "reactivemongo-akkastream" % "0.12.7",
    "org.scalaz"           %% "scalaz-core"              % scalazVersion,
    "org.scalaz"           %% "scalaz-concurrent"        % scalazVersion,
    "com.chuusai"          %% "shapeless"                % "2.3.2",
    "com.lihaoyi"          %% "fastparse"                % "0.4.2",
    "com.vmunier"          %% "scalajs-scripts"          % "1.1.1",
    "org.typelevel"        %% "cats"                     % "0.9.0",
    "com.mohiva"           %% "play-html-compressor"     % "0.7.1",
    "com.typesafe.play"    %% "play-json"                % "2.6.3",
    "com.github.dfabulich" % "sitemapgen4j"              % "1.0.6"
  )

  lazy val testDeps = Seq(
    "com.typesafe.akka"        %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka"        %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"  % "3.1.2"     % Test,
    "org.mockito"              % "mockito-core"         % "2.11.0"    % Test,
    "com.softwaremill.macwire" %% "macros"              % "2.3.0"     % Test,
    "org.awaitility"           % "awaitility"           % "3.0.0"     % Test
  )

  lazy val frontendDeps = Seq(
    "org.webjars"       %% "webjars-play"       % "2.6.1",
    "org.webjars"       % "jquery"              % "3.2.1",
    "org.webjars.bower" % "jquery.lazyload"     % "1.9.7",
    "org.webjars"       % "jquery-ui"           % "1.12.1", // css included over cdn
    "org.webjars.npm"   % "foundation-sites"    % "6.4.3",
    "org.webjars.npm"   % "mithril"             % "0.2.8", // 1.1.3 available
    "org.webjars.bower" % "d3"                  % "4.10.2",
    "org.webjars.npm"   % "slick-carousel"      % "1.6.0",
    "org.webjars.npm"   % "codemirror-minified" % "5.28.0",
    "org.webjars.bower" % "clipboard"           % "1.7.1", // currently not in use
    "org.webjars.bower" % "datatables"          % "1.10.16",
    "org.webjars"       % "highcharts"          % "5.0.14",
    "org.webjars.bower" % "velocity"            % "1.5.0",
    "org.webjars"       % "font-awesome"        % "4.7.0",
    "org.webjars"       % "select2"             % "4.0.3",
    "org.webjars.npm"   % "tooltipster"         % "4.2.5",
    "org.webjars"       % "momentjs"            % "2.18.1"
  )

}
