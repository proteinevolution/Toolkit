lazy val akkaVersion = "2.4.17"

lazy val commonDeps = Seq(ws, filters, cache,
  "com.typesafe.akka"     %% "akka-actor"               % akkaVersion,
  "com.sanoma.cda"        %% "maxmind-geoip2-scala"     % "1.5.4",
  "com.typesafe.akka"     %% "akka-cluster"             % akkaVersion,
  "com.typesafe.akka"     %% "akka-cluster-tools"       % akkaVersion,
  "com.typesafe.akka"     %% "akka-cluster-metrics"     % akkaVersion,
  "com.typesafe.akka"     %% "akka-slf4j"               % akkaVersion,
  "com.typesafe.akka"     %% "akka-stream"              % akkaVersion,
  "com.typesafe.akka"     %% "akka-persistence"         % akkaVersion,
  "com.typesafe.play"     %% "play-mailer"              % "5.0.0",
  "com.github.pathikrit"  %% "better-files"             % "2.17.1",
  "org.mindrot"            % "jbcrypt"                  % "0.3m",
  "com.evojam"            %% "play-elastic4s"           % "0.3.1",
  "org.reactivemongo"     %% "play2-reactivemongo"      % "0.12.2",
  "org.reactivemongo"     %% "reactivemongo-play-json"  % "0.12.2",
  "co.fs2"                %% "fs2-core"                 % "0.9.2",
  "org.scalaz"            %% "scalaz-core"              % "7.2.10",
  "org.scalaz"            %% "scalaz-concurrent"        % "7.2.10",
  "com.chuusai"           %% "shapeless"                % "2.3.2",
  "org.clapper"           %% "classutil"                % "1.0.11",
  "com.beachape"          %% "enumeratum"               % "1.4.13",
  "com.beachape"          %% "enumeratum-play"          % "1.4.13",
  "com.beachape"          %% "enumeratum-play-json"     % "1.4.13",
  "net.ruippeixotog"      %% "scala-scraper"            % "1.1.0",
  "com.lihaoyi"           %% "fastparse"                % "0.4.1",
  "com.vmunier"           %% "scalajs-scripts"          % "1.0.0",
  "org.typelevel"         %% "cats"                     % "0.8.1",
  "com.mohiva"            %% "play-html-compressor"     % "0.6.3"
)



/*
 * Settings which apply to all modules of this application
 */
lazy val commonSettings = Seq(
  version := "0.0.0",
  scalaVersion := "2.11.8",
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  logLevel := Level.Warn,
  dependencyOverrides ++= Set("org.webjars" % "jquery" % "3.1.1",
                              "com.typesafe.akka" %% "akka-actor" % akkaVersion))

lazy val metadata = List(
  organization := "ebio.tuebingen.mpg.de",
  developers   := List(
    Developer("JoKuebler", "Jonas Kuebler", "jkuebler@tuebingen.mpg.de", url("https://github.com/JoKuebler")),
    Developer("zy4", "Seung-Zin Nam", "seungzin.nam@tuebingen.mpg.de", url("https://github.com/zy4")),
    Developer("davidrau", "David Rau", "drau@tuebingen.mpg.de", url("https://github.com/davidmrau")),
    Developer("anjestephens", "Andrew Jesse Stephens", "astephens@tuebingen.mpg.de", url("https://github.com/anjestephens")),
    Developer("lkszmn", "Lukas Zimmermann", "lukas.zimmermann@tuebingen.mpg.de", url("https://github.com/lkszmn")),
    Developer("markolozajic", "Marko Lozajic", "marko.lozajic@tuebingen.mpg.de", url("https://github.com/markolozajic"))
  )
)


lazy val root = (project in file("."))
  .enablePlugins(PlayScala, JavaAppPackaging, SbtWeb)
  .settings(
    commonSettings,
    name := "mpi-toolkit",
    libraryDependencies ++= (commonDeps ++ Seq(
      "org.webjars"        %% "webjars-play"          % "2.5.0-3",
      "org.webjars"         % "jquery"                % "3.1.1",
      "org.webjars.bower"   % "jquery.lazyload"       % "1.9.7",
      "org.webjars"         % "jquery-ui"             % "1.12.1",
      "org.webjars.npm"     % "foundation-sites"      % "6.3.1",
      "org.webjars.bower"   % "fastclick"             % "1.0.6",
      "org.webjars.npm"     % "mithril"               % "0.2.5",
      "org.webjars.bower"   % "d3"                    % "4.4.1",
      "org.webjars.bower"   % "slick-carousel"        % "1.6.0",
      "org.webjars.npm"     % "codemirror-minified"   % "5.22.0",
      //"org.webjars.npm"     % "reformat.js"           % "0.1.9",
      "org.webjars"         % "dropzone"              % "4.3.0",
      "org.webjars.bower"   % "clipboard"             % "1.5.10",
      "org.webjars"         % "linkurious.js"         % "1.5.1",
      "org.webjars.bower"   % "tinymce"               % "4.5.5",
      "org.webjars.bower"   % "datatables"            % "1.10.13",
      "org.webjars"         % "highcharts"            % "5.0.6",
      "org.webjars.bower"   % "velocity"              % "1.5.0",
      "org.webjars"         % "font-awesome"          % "4.7.0",
      "org.webjars.npm"     % "justgage"              % "1.2.2")),
    pipelineStages := Seq(rjs, digest, gzip),
    compile in Compile <<= (compile in Compile) dependsOn scalaJSPipeline,
    sassOptions in Assets ++= Seq("--compass", "-r", "compass"),
    sassOptions in Assets ++= Seq("--cache-location", "target/web/sass/.sass-cache")
  )


ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
resolvers += "Madoushi sbt-plugins" at "https://dl.bintray.com/madoushi/sbt-plugins/"

lazy val client = (project in file("client")).settings(
  scalaVersion := "2.11.8",
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js"  %%% "scalajs-dom"       % "0.9.1",
    "co.technius"   %%% "scalajs-mithril"   % "0.1.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb)


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

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node