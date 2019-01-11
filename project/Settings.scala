import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import sbt.Version

object Settings {

  lazy val release = taskKey[Unit]("Release task")
  lazy val postRelease = taskKey[Unit]("Post release ефыл")

  lazy val settings = Seq(
    organization := "com.onefactor.sbt",
    scalaVersion := "2.12.0",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    release := {
      val v = Version(version.value).get
      val releaseVersion = v.withoutQualifier.string
      println(releaseVersion)
      version := releaseVersion
      println(version.value)
      val gitRunner = git.runner.value
      gitRunner.commitAndPush(s"Release $releaseVersion", Some(releaseVersion))
//      val version = Version("1.3.1-SNAPSHOT").get
//      println(version)
//      val release = version.copy(qualifier = None)
//      println(release.string)
//      val bumpedMinor = version.bumpMinor
//      println(bumpedMinor.string)
    },
    postRelease := {
      println(version.value)
    }
//    releaseProcess := Seq[ReleaseStep](
//      checkSnapshotDependencies,
//      inquireVersions,
//      setReleaseVersion,
//      commitReleaseVersion,
//      tagRelease
//    ),
//    postReleaseProcess := Seq[ReleaseStep](
//      setNextVersion,
//      commitNextVersion,
//      pushChanges
//    ),
  )

  lazy val testSettings = Seq(
    fork in Test := false,
    parallelExecution in Test := false
  )

  lazy val itSettings = Defaults.itSettings ++ Seq(
    logBuffered in IntegrationTest := false,
    fork in IntegrationTest := true
  )

  lazy val myappSettings = Seq(
    assemblyJarName in assembly := "myapp-" + version.value + ".jar",
    test in assembly := {},
    target in assembly := file(baseDirectory.value + "/../bin/"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(
      includeScala = false,
      includeDependency=true),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case n if n.startsWith("reference.conf") => MergeStrategy.concat
      case _ => MergeStrategy.first
    }
  )

  lazy val module1Settings = Seq()

  lazy val module2Settings = Seq()

}
