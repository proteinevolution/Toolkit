import sbtbuildinfo.BuildInfoPlugin.autoImport._
import Settings._

inThisBuild(
  Seq(
    organization := "de.proteinevolution",
    scalaVersion := "2.12.8"
  )
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

lazy val coreSettings = Seq(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  TwirlKeys.templateImports := Nil
) ++ Settings.compileSettings ++ Release.settings

import sbtcrossproject.{ crossProject, CrossType }

lazy val common = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("modules/common"))
  .settings(
    name := "de.proteinevolution.common",
    libraryDependencies ++= Dependencies.commonDeps,
    Settings.compileSettings
  )
  .settings(addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)))

lazy val commonJS  = common.js.dependsOn(base, tel)
lazy val commonJVM = common.jvm.dependsOn(base, tel)

lazy val results = (project in file("modules/results"))
  .commonSettings("de.proteinevolution.results")
  .enablePlugins(PlayScala, SbtTwirl)
  .dependsOn(commonJVM, auth, jobs, help, ui, base, tools, util)
  .settings(addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)))
  .settings(addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"))
  .disablePlugins(PlayLayoutPlugin)

lazy val help = (project in file("modules/help"))
  .commonSettings("de.proteinevolution.help")
  .enablePlugins(PlayScala, SbtTwirl)
  .dependsOn(commonJVM, base)
  .disablePlugins(PlayLayoutPlugin)

lazy val jobs = (project in file("modules/jobs"))
  .commonSettings("de.proteinevolution.jobs")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, auth, base, clusterApi, tel, tools, util)
  .settings(addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)))
  .disablePlugins(PlayLayoutPlugin)

lazy val auth = (project in file("modules/auth"))
  .commonSettings("de.proteinevolution.auth")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, tel, util)
  .disablePlugins(PlayLayoutPlugin)

lazy val base = (project in file("modules/base"))
  .commonSettings("de.proteinevolution.base")
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)

lazy val cluster = (project in file("modules/cluster"))
  .commonSettings("de.proteinevolution.cluster")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, jobs, clusterApi)
  .disablePlugins(PlayLayoutPlugin)

lazy val clusterApi = (project in file("modules/cluster-api"))
  .commonSettings("de.proteinevolution.cluster.api")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base)
  .disablePlugins(PlayLayoutPlugin)

lazy val backend = (project in file("modules/backend"))
  .commonSettings("de.proteinevolution.backend")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, auth, jobs, tel, tools)
  .disablePlugins(PlayLayoutPlugin)

lazy val search = (project in file("modules/search"))
  .commonSettings("de.proteinevolution.search")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, auth, jobs, tools)
  .disablePlugins(PlayLayoutPlugin)

lazy val ui = (project in file("modules/ui"))
  .commonSettings("de.proteinevolution.ui")
  .enablePlugins(PlayScala, SbtTwirl, BuildInfoPlugin)
  .dependsOn(commonJVM, base, tools)
  .settings(
    buildInfoSettings
  )
  .disablePlugins(PlayLayoutPlugin)

lazy val message = (project in file("modules/message"))
  .commonSettings("de.proteinevolution.message")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, auth, cluster, jobs, tools)
  .disablePlugins(PlayLayoutPlugin)

lazy val verification = (project in file("modules/verification"))
  .commonSettings("de.proteinevolution.verification")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, base, auth, ui, message, tel, tools)
  .disablePlugins(PlayLayoutPlugin)

lazy val migrations = (project in file("modules/migrations"))
  .commonSettings("de.proteinevolution.migrations")
  .enablePlugins(PlayScala)
  .settings(scalacOptions --= Seq("-Ywarn-unused:imports"))
  .disablePlugins(PlayLayoutPlugin)

lazy val tel = (project in file("modules/tel"))
  .commonSettings("de.proteinevolution.tel")
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)

lazy val tools = (project in file("modules/tools"))
  .commonSettings("de.proteinevolution.tools")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM, params)
  .settings(addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)))
  .disablePlugins(PlayLayoutPlugin)

lazy val util = (project in file("modules/util"))
  .commonSettings("de.proteinevolution.util")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM)
  .disablePlugins(PlayLayoutPlugin)

lazy val params = (project in file("modules/params"))
  .commonSettings("de.proteinevolution.params")
  .enablePlugins(PlayScala)
  .dependsOn(commonJVM)
  .settings(addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.0").cross(CrossVersion.full)))
  .disablePlugins(PlayLayoutPlugin)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayAkkaHttp2Support, JavaAppPackaging, SbtWeb)
  .dependsOn(
    client,
    commonJVM,
    results,
    cluster,
    backend,
    search,
    message,
    verification,
    migrations
  )
  .settings(
    coreSettings,
    name := "mpi-toolkit",
    libraryDependencies ++= (Dependencies.commonDeps ++ Dependencies.testDeps ++ Dependencies.frontendDeps),
    pipelineStages := Seq(digest, gzip),
    compile in Compile := (compile in Compile).dependsOn(scalaJSPipeline).value,
    // TODO: TypescriptKeys.configFile := "/app/app/assets/tsconfig.json"
    // https://github.com/ArpNetworking/sbt-typescript#tsconfigjson-support-version--030
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
