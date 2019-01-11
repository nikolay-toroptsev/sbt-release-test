import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._


object Settings {

//  lazy val release = taskKey[Unit]("Release task")
  lazy val postRelease = taskKey[Unit]("Post release ефыл")

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
//    release := {
//      val v = Version(version.value).get
//      val releaseVersion = v.withoutQualifier.string
//      println(releaseVersion)
//      version := releaseVersion
//      println(version.value)
//      val gitRunner = git.runner.value
//      gitRunner.commitAndPush(s"Release $releaseVersion", Some(releaseVersion))
//    },
//    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    
    //    postRelease := {
    //      println(version.value)
    //      val newVersion = sbtrelease.Version(version.value).get.bumpMinor.asSnapshot
    //      println(newVersion.string)
    //      version := newVersion.string
    //      git.runner.value.commitAndPush("Post release new snapshot version")
    //    }
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
