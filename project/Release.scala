import sbt._
import sbtrelease.ReleaseStateTransformations._

object Release {

  import sbtrelease.ReleasePlugin.autoImport._

  def settings: Setting[Seq[ReleaseStep]] = {
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      // runTest, / tests are run by travis
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }

}
