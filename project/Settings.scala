import sbt._
import Keys.{publishTo, _}
import sbtassembly.AssemblyPlugin.autoImport._


object Settings {

  lazy val settings = Seq(
    organization := "com.onefactor.sbt",
    scalaVersion := "2.12.0",
    publishMavenStyle := true,
    publishArtifact in Test := false
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
    publishTo := {
      if (isSnapshot.value) {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-snapshots;build.timestamp=" + System.currentTimeMillis())
      } else {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-releases")
      }
    }
  )

  lazy val module1Settings = Seq(
    publishTo := {
      if (isSnapshot.value) {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-snapshots;build.timestamp=" + System.currentTimeMillis())
      } else {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-releases")
      }
    }
  )

  lazy val module2Settings = Seq(
    publishTo := {
      if (isSnapshot.value) {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-snapshots;build.timestamp=" + System.currentTimeMillis())
      } else {
        Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-releases")
      }
    }
  )

}
