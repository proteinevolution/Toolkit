import sbt.Keys._
import sbt.{Compile, Def, Project, Setting, Task}

object Settings {

  // find infos about compiler flags at https://docs.scala-lang.org/overviews/compiler-options/index.html
  private val coreFlags = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Wextra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Wnumeric-widen", // Warn when numerics are widened.
    "-Wunused:imports", // Warn if an import selector is not referenced.
    "-Wunused:locals", // Warn if a local definition is unused.
    "-Wunused:privates" // Warn if a private member is unused.
    // "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
    // "-Wunused:implicits",            // Warn if an implicit parameter is unused.
    // "-Wunused:params",               // Warn if a value parameter is unused.
    // "-Wunused:patvars",              // Warn if a variable bound in a pattern is unused.
  )

  private val additionalFlags = Seq(
    "-Wdead-code", // Warn when dead code is identified.
    "-Wvalue-discard" // Warn when non-Unit expression results are unused.
  )

  private val allFlags: Seq[String] = coreFlags ++ additionalFlags

  lazy val compileSettings: Seq[Def.Setting[Task[Seq[String]]]] = Seq(
    scalacOptions ++= allFlags)

  lazy val disableDocs: Seq[Def.Setting[_]] = Seq[Setting[_]](
    Compile / doc / sources := Seq.empty,
    Compile / packageDoc / publishArtifact := false
  )

  implicit class SettingsFromProject(project: Project) {
    import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
    def commonSettings(projectName: String): Project =
      project
        .settings(
          name := projectName,
          libraryDependencies ++= Dependencies.commonDeps,
          Settings.compileSettings,
          disableDocs
        )
        .enablePlugins(AutomateHeaderPlugin)
  }

}
