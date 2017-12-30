import sbtbuildinfo.BuildInfoPlugin.autoImport._

inThisBuild(Seq(
  organization := "de.proteinevolution",
  scalaVersion := "2.12.4"
))

lazy val commonSettings = Seq(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  logLevel := Level.Warn,
  TwirlKeys.templateImports := Nil
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion,
    "playVersion" -> play.core.PlayVersion.current
  ),
  buildInfoPackage := "build"
)

lazy val coreSettings = commonSettings ++ Settings.compileSettings ++ Release.settings

lazy val disableDocs = Seq[Setting[_]](
  sources in (Compile, doc) := Seq.empty,
  publishArtifact in (Compile, packageDoc) := false
)

lazy val common = (project in file("modules/common"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .settings(
      name := "common",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val tools = (project in file("modules/tools"))
    .enablePlugins(PlayScala, JavaAppPackaging, SbtTwirl)
    .dependsOn(common)
    .settings(
      name := "tools",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, PlayAkkaHttp2Support, JavaAppPackaging, SbtWeb, BuildInfoPlugin)
    .dependsOn(client, common, tools)
    .aggregate(client, common, tools)
    .settings(
      coreSettings,
      name := "mpi-toolkit",
      libraryDependencies ++= (Dependencies.commonDeps ++ Dependencies.testDeps ++ Dependencies.frontendDeps),
      pipelineStages := Seq(digest, gzip),
      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
      buildInfoSettings
    )

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val client = (project in file("modules/client"))
    .enablePlugins(ScalaJSPlugin, ScalaJSWeb, BuildInfoPlugin)
    .settings(
      scalaJSUseMainModuleInitializer := true,
      scalaJSUseMainModuleInitializer in Test := false,
      jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
      buildInfoSettings,
      libraryDependencies ++= Seq(
        "org.scala-js"  %%% "scalajs-dom"     % "0.9.3",
        "com.tgf.pizza" %%% "scalajs-mithril" % "0.1.1",
        "be.doeraene"   %%% "scalajs-jquery"  % "0.9.2"
      )
    )

fork := true // required for "sbt run" to pick up javaOptions
javaOptions += "-Dplay.editor=http://localhost:63342/api/file/?file=%s&line=%s"
fork in Test := true
logLevel in Test := Level.Info

scalacOptions in Test ++= Seq("-Yrangepos")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
PlayKeys.devSettings := Seq("play.server.akka.requestTimeout" -> "infinite")
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node
