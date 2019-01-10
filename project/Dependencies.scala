import sbt._
import Keys._

object Dependencies {

  lazy val version = new {
      val scalaTest       = "3.0.0"
      val scalaCheck      = "1.13.4"
  }

  lazy val library = new  {
      val test  = "org.scalatest" %% "scalatest" % version.scalaTest % Test
      val check = "org.scalacheck" %% "scalacheck" % version.scalaCheck % Test
  }

  val module1Dependencies: Seq[ModuleID] = Seq(
    library.test,
    library.check
  )

  val module2Dependencies: Seq[ModuleID] = Seq(
    library.test,
    library.check
  )

  val myappDependencies: Seq[ModuleID] = Seq(
    library.test,
    library.check
  )

}
