import sbtbuildinfo.BuildInfoPlugin.autoImport._

inThisBuild(
  Seq(
    organization := "de.proteinevolution",
    scalaVersion := "2.12.6"
  )
)

lazy val commonSettings = Seq(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  TwirlKeys.templateImports := Nil
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    "scalaJSVersion" -> scalaJSVersion,
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

import sbtcrossproject.{crossProject, CrossType}

lazy val common = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("modules/common"))
  .settings(
    name := "common",
    libraryDependencies ++= Dependencies.commonDeps,
    Settings.compileSettings,
    disableDocs
  )

lazy val commonJS  = common.js
lazy val commonJVM = common.jvm

lazy val results = (project in file("modules/results"))
  .enablePlugins(PlayScala, JavaAppPackaging, SbtTwirl)
  .dependsOn(commonJVM, auth, jobs)
  .settings(
    name := "de.proteinevolution.results",
    libraryDependencies ++= Dependencies.commonDeps,
    Settings.compileSettings,
    TwirlKeys.templateImports := Seq.empty,
    disableDocs
  )
  .disablePlugins(PlayLayoutPlugin)

lazy val help = (project in file("modules/help"))
    .enablePlugins(PlayScala, JavaAppPackaging, SbtTwirl)
    .dependsOn(commonJVM, base)
    .settings(
      name := "de.proteinevolution.help",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val jobs = (project in file("modules/jobs"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(commonJVM, auth, base)
    .settings(
      name := "de.proteinevolution.jobs",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val auth = (project in file("modules/auth"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(commonJVM, base)
    .settings(
      name := "de.proteinevolution.auth",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val base = (project in file("modules/base"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .settings(
      name := "de.proteinevolution.base",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val cluster = (project in file("modules/cluster"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(commonJVM, base, jobs)
    .settings(
      name := "de.proteinevolution.cluster",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val backend = (project in file("modules/backend"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(commonJVM, base, auth, jobs)
    .settings(
      name := "de.proteinevolution.backend",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val search = (project in file("modules/search"))
    .enablePlugins(PlayScala, JavaAppPackaging)
    .dependsOn(commonJVM, base, auth, jobs)
    .settings(
      name := "de.proteinevolution.search",
      libraryDependencies ++= Dependencies.commonDeps,
      Settings.compileSettings,
      TwirlKeys.templateImports := Seq.empty,
      disableDocs
    )
    .disablePlugins(PlayLayoutPlugin)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support, JavaAppPackaging, SbtWeb, BuildInfoPlugin)
  .dependsOn(client, commonJVM, results, jobs, auth, base, cluster, help, backend, search)
  .aggregate(client, commonJVM, results, jobs, auth, base, cluster, help, backend, search)
  .settings(
    coreSettings,
    name := "mpi-toolkit",
    libraryDependencies ++= (Dependencies.commonDeps ++ Dependencies.testDeps ++ Dependencies.frontendDeps),
    pipelineStages := Seq(digest, gzip),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    buildInfoSettings
  )

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val client = (project in file("modules/client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
  .dependsOn(commonJS)
  .settings(
    Settings.sjsCompileSettings,
    scalaJSUseMainModuleInitializer := true,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    scalaJSUseMainModuleInitializer in Test := false,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    libraryDependencies ++= Dependencies.clientDeps.value
  )

fork := true // required for "sbt run" to pick up javaOptions
javaOptions += "-Dplay.editor=http://localhost:63342/api/file/?file=%s&line=%s"
fork in Test := true
logLevel in Test := Level.Info

scalacOptions in Test ++= Seq("-Yrangepos")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

PlayKeys.devSettings := Seq("play.server.http.idleTimeout" -> "220s")
