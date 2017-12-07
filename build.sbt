lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.11.11",
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  logLevel := Level.Warn,
  TwirlKeys.templateImports := Nil
)

lazy val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val headless = (project in file("modules/headless"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(common)
    .settings(
      disableDocs
    )

lazy val common = (project in file("modules/common"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .settings(
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, PlayAkkaHttp2Support, JavaAppPackaging, SbtWeb)
    .dependsOn(client, headless, common)
    .aggregate(client, headless, common)
    .settings(
      commonSettings,
      name := "mpi-toolkit",
      libraryDependencies ++= (Dependencies.commonDeps ++ Dependencies.testDeps ++ Dependencies.frontendDeps),
      pipelineStages := Seq(digest, gzip),
      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
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

lazy val client = (project in file("client"))
    .settings(
      scalaVersion := "2.11.11",
      scalaJSUseMainModuleInitializer := true,
      scalaJSUseMainModuleInitializer in Test := false,
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom"     % "0.9.3",
        "co.technius"  %%% "scalajs-mithril" % "0.1.0",
        "be.doeraene"  %%% "scalajs-jquery"  % "0.9.2"
      )
    )
    .enablePlugins(ScalaJSPlugin, ScalaJSWeb)

fork in run := false
fork in Test := true
logLevel in Test := Level.Info

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)

scalacOptions in Test ++= Seq("-Yrangepos")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
