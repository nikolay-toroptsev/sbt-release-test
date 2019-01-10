import sbt._
import Keys.{publishTo, _}
import sbtassembly.AssemblyPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseProcess, releaseVersionBump}
import sbtrelease.ReleaseStateTransformations._

object Settings {

  lazy val settings = Seq(
    organization := "com.onefactor.sbt",
    scalaVersion := "2.12.0",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := {
      if (isSnapshot.value) {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-snapshots;build.timestamp=" + System.currentTimeMillis())
      } else {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-releases")
      }
    }
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
    },
    releaseVersionBump := sbtrelease.Version.Bump.Next,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  lazy val module1Settings = Seq()

  lazy val module2Settings = Seq()

}
