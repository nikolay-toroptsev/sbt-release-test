package com.onefactor.sbt.myapp

import com.onefactor.sbt.module1
import com.onefactor.sbt.module2


object Myapp extends App {
      val version = Version("1.3.1-SNAPSHOT").get
      println(version)
      val release = version.copy(qualifier = None)
      println(release.string)
      val bumpedMinor = version.bumpMinor
      println(bumpedMinor.string)
}
