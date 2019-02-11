import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.sbt.PlayImport._
import sbt._

object Dependencies {

  val akkaVersion = "2.5.20"
  val catsV       = "1.5.0"
  val circeV      = "0.11.1"

  lazy val commonDeps: Seq[ModuleID] = Seq(
    ws,
    filters,
    ehcache,
    guice,
    "com.typesafe.akka"    %% "akka-actor"               % akkaVersion,
    "com.tgf.pizza"        %% "maxmind-geoip2-scala"     % "1.5.6",
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
    "com.vmunier"          %% "scalajs-scripts"          % "1.1.1",
    "com.mohiva"           %% "play-html-compressor"     % "0.7.1",
    "com.dripower"         %% "play-circe"               % "2711.0",
    "io.circe"             %% "circe-generic"            % circeV,
    "io.circe"             %% "circe-generic-extras"     % circeV,
    "io.circe"             %% "circe-java8"              % circeV,
    "io.circe"             %% "circe-bson"               % "0.2.0",
    "org.webjars"          %% "webjars-play"             % "2.7.0",
    "org.scala-js"         %% "scalajs-stubs"            % scalaJSVersion % "provided",
    "com.github.mpilquist" %% "simulacrum"               % "0.12.0",
    "com.github.mongobee"  % "mongobee"                  % "0.13"
  )

  lazy val clientDeps: Def.Initialize[Seq[sbt.ModuleID]] = Def.setting(
    Seq(
      "org.scala-js"  %%% "scalajs-dom"     % "0.9.6",
      "com.tgf.pizza" %%% "scalajs-mithril" % "1.1.5",
      "be.doeraene"   %%% "scalajs-jquery"  % "0.9.4",
      "org.querki"    %%% "jquery-facade"   % "1.2",
      "org.typelevel" %%% "cats-core"       % catsV,
      "com.lihaoyi"   %%% "upickle"         % "0.6.7"
    )
  )

  lazy val testDeps: Seq[ModuleID] = Seq(
    "com.typesafe.akka"        %% "akka-testkit"        % akkaVersion % Test,
    "com.typesafe.akka"        %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatestplus.play"   %% "scalatestplus-play"  % "4.0.1"     % Test,
    "org.mockito"              % "mockito-core"         % "2.11.0"    % Test,
    "com.softwaremill.macwire" %% "macros"              % "2.3.1"     % Test,
    "org.awaitility"           % "awaitility"           % "3.0.0"     % Test
  )

  lazy val frontendDeps: Seq[ModuleID] = Seq(
    "org.webjars"       % "jquery"                               % "3.3.1",
    "org.webjars.bower" % "jquery.lazyload"                      % "1.9.7",
    "org.webjars"       % "jquery-ui"                            % "1.12.1",
    "org.webjars.npm"   % "foundation-sites"                     % "6.4.3",
    "org.webjars.npm"   % "mithril"                              % "0.2.8",
    "org.webjars.npm"   % "codemirror-minified"                  % "5.28.0",
    "org.webjars.bower" % "datatables"                           % "1.10.16",
    "org.webjars"       % "highcharts"                           % "6.0.7",
    "org.webjars.bower" % "velocity"                             % "1.5.0",
    "org.webjars"       % "font-awesome"                         % "5.0.6",
    "org.webjars"       % "select2"                              % "4.0.3",
    "org.webjars.npm"   % "tooltipster"                          % "4.2.5",
    "org.webjars"       % "momentjs"                             % "2.21.0",
    "org.webjars.bower" % "typeahead.js"                         % "0.11.1",
    "org.webjars.npm"   % "snapsvg"                              % "0.5.1",
    "org.webjars.npm"   % "jquery-nice-select"                   % "1.1.0",
    "org.webjars.npm"   % "floating-scroll"                      % "2.3.3",
    "org.webjars.npm"   % "biojs-io-newick"                      % "1.5.0",
    "org.webjars.bower" % "df-visible"                           % "1.3.0-rc.1",
    "org.webjars.npm"   % "ngl"                                  % "0.10.4",
    "org.webjars.npm"   % "hamburgers"                           % "0.9.1",
    "org.webjars.npm"   % "gasparesganga-jquery-loading-overlay" % "2.1.2"
  )

}
