import Dependencies._
import Settings._

lazy val module1 = (project in file("module1")).
  settings(Settings.settings: _*).
  settings(Settings.module1Settings: _*).
  settings(libraryDependencies ++= module1Dependencies)

lazy val module2 = (project in file("module2")).
  settings(Settings.settings: _*).
  settings(Settings.module2Settings: _*).
  settings(libraryDependencies ++= module2Dependencies)

lazy val myapp = (project in file("myapp")).
  settings(Settings.settings: _*).
  settings(Settings.myappSettings: _*).
  settings(publishTo := {
    if (isSnapshot.value) {
      Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-snapshots;build.timestamp=" + System.currentTimeMillis())
    } else {
      Some("Artifactory Realm" at "https://onef.jfrog.io/onef/dl-private-releases")
    }
  }).
  dependsOn(module1, module2).
  configs(Test)
