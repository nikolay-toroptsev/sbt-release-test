import com.typesafe.sbt.GitPlugin
import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.releaseVersionFile
import sbtrelease.ReleaseStateTransformations.globalVersionString

object PostReleasePlugin extends AutoPlugin {
  override def requires: Plugins = GitPlugin && ReleasePlugin

  object autoImport {
    val postRelease = taskKey[Unit]("Post release task")
  }

  import autoImport._
  
  override def projectSettings: Seq[Setting[_]] = Seq(
    postRelease := {
      val releaseVersion = (version in ThisBuild).value
      println(s"Release version: $releaseVersion")
      val nextVersion = sbtrelease.Version(releaseVersion).get.bumpMinor.asSnapshot.string
      println(s"Next snapshot version: $nextVersion")
      val versionFile = Project.extract((state in ThisBuild).value).get(releaseVersionFile)
      IO.writeLines(versionFile, Seq(globalVersionString format nextVersion))
      version in ThisBuild := nextVersion
      git.runner.value.commitAndPush("Post release new snapshot version")(file("."), ConsoleLogger())
    }
  )
}
