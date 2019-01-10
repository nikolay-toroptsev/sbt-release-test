import Dependencies._

lazy val module1 = (project in file("module1"))
  .enablePlugins(ReleasePlugin)
  .settings(Settings.settings: _*)
  .settings(Settings.module1Settings: _*)
  .settings(libraryDependencies ++= module1Dependencies)

lazy val module2 = (project in file("module2"))
  .enablePlugins(ReleasePlugin)
  .settings(Settings.settings: _*)
  .settings(Settings.module2Settings: _*)
  .settings(libraryDependencies ++= module2Dependencies)

lazy val myapp = (project in file("myapp"))
  .settings(Settings.settings: _*)
  .settings(Settings.myappSettings: _*)
  .dependsOn(module1, module2)
  .configs(Test)
