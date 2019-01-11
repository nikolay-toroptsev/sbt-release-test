import Dependencies._
import sbt.Keys._
import sbtrelease.ReleaseStateTransformations._

lazy val postRelease = taskKey[Unit]("Post release task")

lazy val module1 = (project in file("module1"))
  .settings(Settings.settings: _*)
  .settings(Settings.module1Settings: _*)
  .settings(libraryDependencies ++= module1Dependencies)

lazy val module2 = (project in file("module2"))
  .settings(Settings.settings: _*)
  .settings(Settings.module2Settings: _*)
  .settings(libraryDependencies ++= module2Dependencies)

lazy val myapp = (project in file("myapp"))
  .settings(Settings.settings: _*)
  .settings(Settings.myappSettings: _*)
  .dependsOn(module1, module2)
  .configs(Test)

lazy val root = (project in file("."))
  .enablePlugins(ReleasePlugin)
  .settings(Settings.settings: _*)
  .settings(
    releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease
    )
  )
  .settings(
      postRelease := {
          val releaseVersion = (sbt.Keys.version in ThisBuild).value
          println(s"Release version: $releaseVersion")
          val nextVersion = sbtrelease.Version(releaseVersion).get.bumpMinor.asSnapshot.string
          println(s"Next snapshot version: $nextVersion")
          val versionFile = Project.extract((state in ThisBuild).value).get(releaseVersionFile)
          IO.writeLines(versionFile, Seq(globalVersionString format nextVersion))
          sbt.Keys.version in ThisBuild := nextVersion
          git.runner.value.commitAndPush("Post release new snapshot version")(file("."), ConsoleLogger())
      }
  )
  .settings(Settings.settings: _*)